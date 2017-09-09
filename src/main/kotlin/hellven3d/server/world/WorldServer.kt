package hellven3d.server.world

import hellven3d.server.lazyLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

class WorldServer(
		private val clientPort: Int,
		private val loginServerAddress: String,
		private val loginServerPort: Int
) {

	class TCPServerComponent {
		val acceptGroup = NioEventLoopGroup()
		val clientGroup = NioEventLoopGroup()

		val bootstrap = ServerBootstrap()
				.group(acceptGroup, clientGroup)
				.channel(NioServerSocketChannel::class.java)
				.childHandler(object : ChannelInitializer<NioSocketChannel>() {
					override fun initChannel(ch: NioSocketChannel) {

					}
				})


	}


	val tcpServer = TCPServerComponent()

	private val players = hashMapOf<Int, Character>()


	fun start() {

	}


	fun stop() {

	}

	companion object {
		val logger by lazyLogger()
	}
}

