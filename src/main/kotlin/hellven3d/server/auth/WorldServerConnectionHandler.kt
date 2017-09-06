package hellven3d.server.auth

import hellven3d.net.InternalPOJO
import hellven3d.net.RegisterWorldServer
import hellven3d.server.lazyLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

internal class WorldServerConnectionHandler(
		private val loginServer: LoginServer
) : SimpleChannelInboundHandler<InternalPOJO>() {


	override fun channelRead0(ctx: ChannelHandlerContext, packet: InternalPOJO?) {
		logger.trace("Received packet from WorldServer: $packet")

		val remoteAddress = ctx.channel().remoteAddress().toString()

		if (packet == null) {
			logger.error("[$remoteAddress] Received null packet from WorldServer.")
		}

		when (packet) {
			is RegisterWorldServer -> {
				// TODO check if the packet is OK
				// TODO check if its a valid server
				// TODO register and add to collection in LoginServer


			}
		}

	}


	companion object {
		internal val logger by lazyLogger()
	}
}
