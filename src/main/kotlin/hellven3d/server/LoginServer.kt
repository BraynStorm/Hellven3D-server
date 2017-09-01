@file:Suppress("NOTHING_TO_INLINE")

package hellven3d.server

import InternalPOJO
import POJO
import hellven3d.server.net.JsonPOJODecoder
import hellven3d.server.net.JsonPOJOEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.json.JsonObjectDecoder
import java.sql.SQLException
import java.util.*

class LoginServer(private val loginServerPort: Int, private val worldServerPort: Int) {
	private val logger by lazyLogger()

	private val clientListenerAcceptGroup = NioEventLoopGroup()
	private val clientListenerConnectionGroup = NioEventLoopGroup()


	private val worldListenerAcceptGroup = NioEventLoopGroup()
	private val worldListenerConnectionGroup = NioEventLoopGroup()

	private val clientServer = ServerBootstrap().also {
		it.group(clientListenerAcceptGroup, clientListenerConnectionGroup)
		it.channel(NioServerSocketChannel::class.java)
		it.childHandler(object : ChannelInitializer<NioSocketChannel>() {
			override fun initChannel(ch: NioSocketChannel) {
				ch.pipeline().addLast(JsonObjectDecoder())
				ch.pipeline().addLast(JsonPOJOEncoder())
				ch.pipeline().addLast(JsonPOJODecoder(POJO::class.java))
				ch.pipeline().addLast(LoginServerConnectionHandler(this@LoginServer))
			}

		})
	}

	private val worldListener = ServerBootstrap().also {
		it.group(worldListenerAcceptGroup, worldListenerConnectionGroup)
		it.channel(NioServerSocketChannel::class.java)
		it.childHandler(object : ChannelInitializer<NioSocketChannel>() {
			//
			override fun initChannel(ch: NioSocketChannel) {
				ch.pipeline().addLast(JsonObjectDecoder())
				ch.pipeline().addLast(JsonPOJOEncoder())
				ch.pipeline().addLast(JsonPOJODecoder(InternalPOJO::class.java))
				ch.pipeline().addLast(WorldServerConnectionHandler(this@LoginServer))
			}

		})
	}

	private var loginServerFuture: ChannelFuture? = null
	private var worldListenerFuture: ChannelFuture? = null

	internal val connections = Collections.synchronizedMap(hashMapOf<Channel, Account>())

	fun start() {
		clientServer.bind(loginServerPort).addListener {
			if (!it.isSuccess) {
				if (it.cause() != null) {
					logger.error("LoginServer can't be opened on port $loginServerPort", it.cause())
				} else {
					logger.error("LoginServer can't be opened on port $loginServerPort")
				}
			} else {
				logger.info("LoginServer opened normally on port $loginServerPort")

//				loginServerFuture = (clientServer.chann).closeFuture().addListener {
//					connections.forEach {
//						DB.setAccountLoggedIn(it.value.id, false)
//					}
//				}
			}
		}

		worldListenerFuture = worldListener.bind(worldServerPort).addListener {
			if (!it.isSuccess) {
				if (it.cause() != null) {
					logger.error("WorldListener  can't be opened on port $worldServerPort.", it.cause())
				} else {
					logger.error("WorldListener can't be opened on port $worldServerPort.")
				}
			} else {
				logger.info("WorldListener opened normally on port $worldServerPort")
//				worldListenerFuture = (it.get() as Channel).closeFuture().addListener {
//
//
//				}
			}
		}
	}

	fun stop() {
		loginServerFuture?.cancel(true)
		worldListenerFuture?.cancel(true)
	}
}

class WorldServerConnectionHandler(private val loginServer: LoginServer) : SimpleChannelInboundHandler<InternalPOJO>() {
	override fun channelRead0(ctx: ChannelHandlerContext, msg: InternalPOJO) {

	}
}

class LoginServerConnectionHandler(private val loginServer: LoginServer) : SimpleChannelInboundHandler<POJO>() {
	companion object {
		val logger by lazyLogger()
	}

	override fun channelActive(ctx: ChannelHandlerContext) {
		ctx.writeAndFlush(POJO.LoginServerInfo().also { it.millis = System.currentTimeMillis() })
	}

	override fun channelRead0(ctx: ChannelHandlerContext, packet: POJO?) {
		val remoteAddress = ctx.channel().remoteAddress().toString()
		logger.debug("[$remoteAddress] Read packet $packet")

		if (packet == null) {
			logger.warn("[$remoteAddress] Recieved NULL packet")

		}


		when (packet) {
			is POJO.AuthStart -> {
				if (packet.email == null || packet.password == null) {
					logger.warn("[$remoteAddress] Malformed POJO")
					// TODO PUNISH
					ctx.close()
					return
				}

				val accountAndStatusPair: Pair<Account?, Boolean> = try {
					DB.tryGetAccount(packet.email, packet.password)
				} catch (e: SQLException) {
					logger.warn("[$remoteAddress] SQL Exception when trying to get account email=${packet.email}, password is not null.")
					null to false
				}

				val account = accountAndStatusPair.first

				if (account == null) {
					sendAuthResult(ctx, POJO.AuthFinished.NO_ACCOUNT)
					logger.info("[$remoteAddress] No such email/password combination")
					return
				}

				val isLoggedIn = accountAndStatusPair.second

				if (isLoggedIn) {
					// FIXME kick the other account too, but for now disallow access.
					// Logged in and logging in again from another client. Kick both
					logger.info("[$remoteAddress] $account is already logged in")
					sendAuthResult(ctx, POJO.AuthFinished.ACCOUNT_ALREADY_LOGGED_IN)

					if (loginServer.connections.containsValue(account)) {
						// TODO kick the other person using the account. To do that, set the flag in the DB to false and notify all WorldServers.
						// TODO what account is in what worldserver. Maybe inthe db or in the login server?
						// TODO what should we use the internal connection for except for tokens?
					}

					// FIXME this line should be removed when the WorldServer is implementd. They will handle this.
					DB.setAccountLoggedIn(account.id, false)
					loginServer.connections.remove(ctx.channel())
					// TODO notify world servers
					return
				}

				loginServer.connections += ctx.channel() to account
				logger.info("$account logged in successfully from ${ctx.channel().remoteAddress()}.")
				sendAuthResult(ctx, POJO.AuthFinished.SUCCESS)
				DB.setAccountLoggedIn(account.id, true)
			}

			is POJO.RequestWorldList -> {
				if (loginServer.connections.containsKey(ctx.channel())) {
					logger.trace("[${ctx.channel().remoteAddress()}] Account is logged in")
					val worldList = DB.getWorldListAndCharacterCount(loginServer.connections[ctx.channel()]?.id ?: return)
					logger.trace("[${ctx.channel().remoteAddress()}] World list acquired")
					ctx.channel().writeAndFlush(worldList)
					logger.trace("[${ctx.channel().remoteAddress()}] World list sent.")
				} else {
					logger.info("[${ctx.channel().remoteAddress()}] Unauthenticated RequestWorldList")
				}
			}

			is POJO.RequestCharacterPrototypeList -> {
				// This packet should return a list of all
				// characters for this account on the requested server

				val account = loginServer.connections[ctx.channel()]

				if (account == null) {
					logger.warn("[${ctx.channel().remoteAddress()}] Unauthenticated RequestCharacterPrototypeList")
					// TODO PUNISH and send Unauthorized packet.
					ctx.close()
					return
				}

				if (packet.world == null) {
					logger.warn("[${ctx.channel().remoteAddress()}] Malformed POJO")
					// TODO PUNISH
					ctx.close()
					return
				}

				// everything is fine
				logger.trace("[$remoteAddress] Request is fine. Acquireing data.")
				val outgoingPacket = DB.getCharacterPrototypeList(account.id, packet.world)
				logger.trace("[$remoteAddress] Sending outgoingPacket")
				ctx.channel().writeAndFlush(outgoingPacket)
			}
		}


	}

	override fun channelInactive(ctx: ChannelHandlerContext) {
		super.channelInactive(ctx)

		val account = loginServer.connections[ctx.channel()] ?: return
		logger.info("$account logged out.")
		DB.setAccountLoggedIn(account.id, false)
	}

	private inline fun sendAuthResult(ctx: ChannelHandlerContext, status: Int) {
		logger.trace("Sending to ${ctx.channel().remoteAddress()}")
		ctx.writeAndFlush(POJO.AuthFinished().also { it.status = status })
	}
}
