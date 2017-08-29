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
//				ch.pipeline().addLast(JsonPOJOCodec())
				ch.pipeline().addLast(JsonPOJOEncoder())
				ch.pipeline().addLast(JsonPOJODecoder())
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

	internal val connections = hashMapOf<Channel, Account>()

	fun start() {
		loginServerFuture = clientServer.bind(clientPort).addListener {
			if (!it.isSuccess)
				throw it.cause()
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

class LoginServerConnectionHandler(private val loginServer: LoginServer) : SimpleChannelInboundHandler<Any>() {
	override fun channelActive(ctx: ChannelHandlerContext) {
		ctx.writeAndFlush(POJO.LoginServerInfo().also { it.millis = System.currentTimeMillis() })
	}

	override fun channelRead0(ctx: ChannelHandlerContext, packet: Any) {
		println("Read packet $packet")

		when (packet) {
			is POJO.AuthStart -> {
				if (packet.email == null || packet.password == null) {
					// TODO LOG AND KICK AND PUNISH
					ctx.close()
					return
				}

				val accountAndStatusPair = DB.tryGetAccount(packet.email, packet.password)

				val account = accountAndStatusPair.first
				val isLoggedIn = accountAndStatusPair.second

				if (account == null) {
					sendAuthResult(ctx, POJO.AuthFinished.NO_ACCOUNT)
					return
				}

				if (isLoggedIn) {
					// TODO kick the other account too, but for now disallow access.
					// Logged in and logging in again from another client. Kick both
					sendAuthResult(ctx, POJO.AuthFinished.ACCOUNT_ALREADY_LOGGED_IN)
					return
				}

				loginServer.connections += ctx.channel() to account
				sendAuthResult(ctx, POJO.AuthFinished.SUCCESS)
			}

			is POJO.RequestWorldList -> {
				val worldList = DB.getWorldList()

			}
		}


	}
	override fun channelInactive(ctx: ChannelHandlerContext?) {
		super.channelInactive(ctx)
	}

	private inline fun sendAuthResult(ctx: ChannelHandlerContext, status: Int) {
		ctx.writeAndFlush(POJO.AuthFinished().also { it.status = status })
	}
}
