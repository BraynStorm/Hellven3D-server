package hellven3d.server.auth

import hellven3d.net.*
import hellven3d.server.Account
import hellven3d.server.DB
import hellven3d.server.lazyLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.sql.SQLException

class LoginServerConnectionHandler(private val loginServer: LoginServer) : SimpleChannelInboundHandler<ExternalPOJO>() {
	companion object {
		val logger by lazyLogger()
	}

	override fun channelActive(ctx: ChannelHandlerContext) {
		ctx.writeAndFlush(LoginServerStatus(System.currentTimeMillis()))
	}

	override fun channelRead0(ctx: ChannelHandlerContext, packet: ExternalPOJO?) {
		val remoteAddress = ctx.channel().remoteAddress().toString()
		logger.debug("[$remoteAddress] Read packet $packet")

		if (packet == null) {
			logger.warn("[$remoteAddress] Received NULL packet")
		}


		when (packet) {
			is AuthStart -> {

				if (synchronized(loginServer.connections) { loginServer.connections.containsKey(ctx.channel()) }) {
					// The person is already logged in from the same connection and is trying to login again.
					// Not to be confued with "Double Logging" - being logged in from a DIFFERENT connection.
					logger.warn("[$remoteAddress] Channel attempted to re-authenticate.")
					// TODO PUNISH
					ctx.close()
				}

				val accountAndStatusPair: Pair<Account?, Boolean> = try {
					DB.tryGetAccount(packet.email, packet.password)
				} catch (e: SQLException) {
					logger.warn("[$remoteAddress] SQL Exception when trying to get account email=${packet.email}, password is not null.")
					null to false
				}

				val account = accountAndStatusPair.first

				if (account == null) {
					sendAuthResult(ctx, AuthFinished.NO_SUCH_ACCOUNT)
					logger.info("[$remoteAddress] No such email/password combination")
					return
				}

				val isAlreadyLoggedIn = accountAndStatusPair.second

				if (isAlreadyLoggedIn) {
					// FIXME kick the other account too, but for now disallow access.
					// Logged in and logging in again from another client. Kick both
					logger.info("[$remoteAddress] $account is already logged in")
					sendAuthResult(ctx, AuthFinished.ACCOUNT_ALREADY_LOGGED_IN)

					val potentialOtherAccount = synchronized(loginServer.connections) {
						loginServer.connections.filter {
							it.value.id == account.id
						}
					}

					if (potentialOtherAccount.isNotEmpty()) {
						potentialOtherAccount.entries.first()
					}

					if (synchronized(loginServer.connections) { loginServer.connections.containsValue(account) }) {
						// TODO kick the other person using the account.
						// TODO To do that, set the flag in the DB to false and notify all WorldServers.
						// TODO what account is in what WorldServer. Maybe in the db or in the login server?
						// TODO what should we use the internal connection for except for tokens?
					}

					// FIXME this line should be removed when the WorldServer is implemented. They will handle this.
					DB.setAccountLoggedIn(account.id, false)

					synchronized(loginServer.connections) {
						loginServer.connections.remove(ctx.channel())
					}
					// TODO notify world servers
					return
				}

				synchronized(loginServer.connections) {
					loginServer.connections += ctx.channel() to account
				}

				logger.info("$account logged in successfully from $remoteAddress.")
				sendAuthResult(ctx, AuthFinished.SUCCESSFUL)
				DB.setAccountLoggedIn(account.id, true)
			}

			is RequestWorldList -> {
				val worldList = synchronized(loginServer.connections) {
					if (loginServer.connections.containsKey(ctx.channel())) {
						logger.trace("[$remoteAddress] Account is logged in")
						DB.getWorldListAndCharacterCount(loginServer.connections[ctx.channel()]?.id ?: return)
					} else {
						null
					}
				}

				if (worldList == null) {
					warnNotAuthenticated(remoteAddress, packet)
					// TODO punish
					ctx.close()
				} else {
					logger.trace("[$remoteAddress] World list acquired")
					ctx.channel().writeAndFlush(worldList)
					logger.trace("[$remoteAddress] World list sent.")
				}
			}

			is RequestCharacterList -> {
				// This packet should return a list of all
				// characters for this account on the requested server

				val account = synchronized(loginServer.connections) { loginServer.connections[ctx.channel()] }

				if (account == null) {
					warnNotAuthenticated(remoteAddress, packet)
					// TODO PUNISH and send Unauthorized packet.
					ctx.close()
					return
				}

				logger.trace("[$remoteAddress] Request is fine. Acquiring data.")
				val outgoingPacket = DB.getCharacterPrototypeList(account.id, packet.worldID)
				logger.trace("[$remoteAddress] Sending outgoingPacket")
				ctx.channel().writeAndFlush(outgoingPacket)
				logger.trace(outgoingPacket.toString())
			}
		}


	}


	private inline fun warnMalformedPOJO(remoteAddress: String, pojo: ExternalPOJO) {
		logger.warn("[$remoteAddress] Malformed POJO: $pojo")
	}

	private inline fun warnNotAuthenticated(remoteAddress: String, pojo: ExternalPOJO) {
		logger.warn("[$remoteAddress] Not authenticated: $pojo")
	}


	override fun channelInactive(ctx: ChannelHandlerContext) {
		super.channelInactive(ctx)
		synchronized(loginServer.connections) {
			val account = loginServer.connections.remove(ctx.channel()) ?: return

			logger.info("$account logged out.")
			DB.setAccountLoggedIn(account.id, false)
		}
	}

	private inline fun sendAuthResult(ctx: ChannelHandlerContext, status: Int) {
		logger.trace("Sending to ${ctx.channel().remoteAddress()}")
		ctx.writeAndFlush(AuthFinished(status))
	}
}
