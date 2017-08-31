@file:Suppress("NOTHING_TO_INLINE")

package braynstorm.hellven3d.server

import InternalPOJO
import POJO
import braynstorm.hellven3d.server.net.JsonPOJODecoder
import braynstorm.hellven3d.server.net.JsonPOJOEncoder
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
				ch.pipeline().addLast(JsonPOJODecoder(POJO::class.java))
				ch.pipeline().addLast(JsonPOJOEncoder(POJO::class.java))
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
				ch.pipeline().addLast(JsonPOJODecoder(InternalPOJO::class.java))
				ch.pipeline().addLast(JsonPOJOEncoder(InternalPOJO::class.java))
				ch.pipeline().addLast(WorldServerConnectionHandler(this@LoginServer))
			}

		})
	}

	private lateinit var loginServerFuture: ChannelFuture
	private lateinit var worldListenerFuture: ChannelFuture

	internal val connections = Collections.synchronizedMap(hashMapOf<Channel, Account>())

	fun start() {
		loginServerFuture = clientServer.bind(loginServerPort).addListener {
			if (!it.isSuccess) {
				if (it.cause() != null) {
					logger.error("LoginServer can't be opened.", it.cause())
				} else {
					logger.error("LoginServer can't be opened on port $loginServerPort")
				}
			} else {
				logger.info("LoginServer opened normally")
			}
		}

		worldListenerFuture = worldListener.bind(worldServerPort).addListener {
			if (!it.isSuccess) {
				if (it.cause() != null) {
					logger.warn("WorldListener  can't be opened.", it.cause())
				} else {
					logger.warn("WorldListener can't be opened.")
				}
			} else {
				logger.info("WorldListener opened normally on port $worldServerPort")
			}
		}
	}

	fun stop() {
		loginServerFuture.cancel(true)
		worldListenerFuture.cancel(true)
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

	override fun channelRead0(ctx: ChannelHandlerContext, packet: POJO) {
		logger.info("Read packet $packet")

		when (packet) {
			is POJO.AuthStart -> {
				if (packet.email == null || packet.password == null) {
					// TODO LOG AND KICK AND PUNISH
					logger.warn("Empty email/password")
					ctx.close()
					return
				}

				var accountAndStatusPair: Pair<Account?, Boolean> = null to false
				try {
					accountAndStatusPair = DB.tryGetAccount(packet.email, packet.password)
				} catch (e: SQLException) {
					e.printStackTrace()
				}
				val account = accountAndStatusPair.first
				val isLoggedIn = accountAndStatusPair.second

				if (account == null) {
					sendAuthResult(ctx, POJO.AuthFinished.NO_ACCOUNT)
					logger.info("No such email/password combination")
					return
				}

				if (isLoggedIn) {
					// TODO kick the other account too, but for now disallow access.
					// Logged in and logging in again from another client. Kick both
					sendAuthResult(ctx, POJO.AuthFinished.ACCOUNT_ALREADY_LOGGED_IN)
					DB.setAccountLoggedIn(account.id, false)
					loginServer.connections.remove(ctx.channel())
					// TODO notify world servers
					logger.info("Already logged in")
					return
				}

				loginServer.connections += ctx.channel() to account
				logger.info(ctx.channel().toString())
				sendAuthResult(ctx, POJO.AuthFinished.SUCCESS)
				DB.setAccountLoggedIn(account.id, true)
				logger.info("All good")

			}

			is POJO.RequestWorldList -> {
				logger.info(ctx.channel().toString())
				if (loginServer.connections.containsKey(ctx.channel())) {
					logger.info("Account is logged in")
					val worldList = DB.getWorldListAndCharacterCount(loginServer.connections[ctx.channel()]?.id ?: return)
					logger.info("World list acquired")
					ctx.channel().writeAndFlush(worldList)
					logger.info("World list sent. $worldList")
				}
			}
		}


	}

	override fun channelInactive(ctx: ChannelHandlerContext) {
		super.channelInactive(ctx)

		val account = loginServer.connections[ctx.channel()] ?: return

		DB.setAccountLoggedIn(account.id, false)


	}

	private inline fun sendAuthResult(ctx: ChannelHandlerContext, status: Int) {
		ctx.writeAndFlush(POJO.AuthFinished().also { it.status = status })
	}
}
