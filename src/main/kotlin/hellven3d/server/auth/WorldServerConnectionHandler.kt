package hellven3d.server.auth

import InternalPOJO
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

internal class WorldServerConnectionHandler(private val loginServer: LoginServer) : SimpleChannelInboundHandler<InternalPOJO>() {
	override fun channelRead0(ctx: ChannelHandlerContext, msg: InternalPOJO) {

	}

}
