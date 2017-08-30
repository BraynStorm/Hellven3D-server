package braynstorm.hellven3d.server

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

class LoginServer(private val clientPort: Int, private val worldServerPort: Int) {

	private val clientListenerAcceptGroup = NioEventLoopGroup()
	private val clientListenerConnectionGroup = NioEventLoopGroup()

	/**
	 * Used to listen for world servers.
	 */
	private val internalListenerAcceptGroup = NioEventLoopGroup()
	private val internalListenerConnectionGroup = NioEventLoopGroup()

	private val clientServer = ServerBootstrap().also {
		it.group(clientListenerAcceptGroup, clientListenerConnectionGroup)
		it.channel(NioServerSocketChannel::class.java)
		it.childHandler(object : ChannelInitializer<NioSocketChannel>() {
			override fun initChannel(ch: NioSocketChannel) {
				ch.pipeline().addLast(JsonObjectDecoder())
				ch.pipeline().addLast(JsonPOJODecoder())
				ch.pipeline().addLast(JsonPOJOEncoder())
				ch.pipeline().addLast(LoginServerConnectionHandler(this@LoginServer))
			}

		})
	}

//	private val worldServer = ServerBootstrap().also {
//		it.group(internalListenerAcceptGroup, internalListenerConnectionGroup)
//		it.channel(NioServerSocketChannel::class.java)
//		it.childHandler(object : ChannelInitializer<NioSocketChannel>() {
//			//
//			override fun initChannel(ch: NioSocketChannel) {
//				ch.pipeline().addLast(JsonObjectDecoder())
//				ch.pipeline().addLast(JsonPOJOCodec())
//				ch.pipeline().addLast(WorldServerConnectionHandler(this@LoginServer))
//			}
//
//		})
//	}

	private lateinit var loginServerFuture: ChannelFuture
	private lateinit var worldServerServerFuture: ChannelFuture

	internal val connections = Collections.synchronizedMap(hashMapOf<Channel, Account>())

	fun start() {
		loginServerFuture = clientServer.bind(clientPort).addListener {
			if (!it.isSuccess)
				throw it.cause()
			else {

			}
		}
	}

	fun stop() {
		loginServerFuture.cancel(true)
	}
}

class WorldServerConnectionHandler(private val loginServer: LoginServer) : CombinedChannelDuplexHandler<SimpleChannelInboundHandler<Any>, ChannelOutboundHandlerAdapter>() {
	override fun read(ctx: ChannelHandlerContext?) {
		super.read(ctx)
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
		println("Read packet $packet")

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
