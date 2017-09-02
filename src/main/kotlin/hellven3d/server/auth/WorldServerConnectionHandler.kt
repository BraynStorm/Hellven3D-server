package hellven3d.server.auth

import hellven3d.net.ExternalPOJO
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

internal class WorldServerConnectionHandler(private val loginServer: LoginServer) : SimpleChannelInboundHandler<ExternalPOJO>() {
	override fun channelRead0(ctx: ChannelHandlerContext, msg: ExternalPOJO) {

	}

}
