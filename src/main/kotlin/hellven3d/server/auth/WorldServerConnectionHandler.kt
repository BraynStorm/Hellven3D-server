package hellven3d.server.auth

import hellven3d.net.InternalPOJO
import hellven3d.net.RegisterWorldServer
import hellven3d.server.DB
import hellven3d.server.lazyLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.net.InetSocketAddress

internal class WorldServerConnectionHandler(
		private val loginServer: LoginServer
) : SimpleChannelInboundHandler<InternalPOJO>() {
	override fun channelActive(ctx: ChannelHandlerContext) {
		val address = ctx.channel().remoteAddress() as InetSocketAddress
		val remoteAddress = address.toString()
		val ip = address.address.hostAddress
		val port = address.port

		val isValid = DB.isWorldServerValid(ip, port)

		if (!isValid) {
			logger.warn("[$remoteAddress] Unknown WorldServer tried to connect to LoginServer")
			ctx.close()
			return
		}


	}

	override fun channelRead0(ctx: ChannelHandlerContext, packet: InternalPOJO?) {
		val remoteAddress = ctx.channel().remoteAddress().toString()

		logger.trace("[$remoteAddress] Received packet from WorldServer: $packet")

		if (packet == null) {
			logger.error("[$remoteAddress] Received null packet from WorldServer.")
			ctx.close()
		}

		when (packet) {
			is RegisterWorldServer -> handleRequestWorldServer(ctx, packet, remoteAddress)
		}

	}

	private fun handleRequestWorldServer(
			ctx: ChannelHandlerContext,
			packet: RegisterWorldServer,
			remoteAddress: String = ctx.channel().remoteAddress().toString()
	) {


	}


	companion object {
		internal val logger by lazyLogger()
	}
}
