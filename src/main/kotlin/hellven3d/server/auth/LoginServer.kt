@file:Suppress("NOTHING_TO_INLINE")

package hellven3d.server.auth

import InternalPOJO
import POJO
import hellven3d.server.Account
import hellven3d.server.lazyLogger
import hellven3d.server.net.JsonPOJODecoder
import hellven3d.server.net.JsonPOJOEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.json.JsonObjectDecoder
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

	// Checks

	fun isAuthenticated(channel: Channel): Boolean = connections.containsKey(channel)
	fun isAuthenticated(account: Account): Boolean = connections.containsValue(account)
}

