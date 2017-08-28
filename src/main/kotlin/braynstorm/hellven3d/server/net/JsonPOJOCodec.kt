package braynstorm.hellven3d.server.net

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec

class JsonPOJOCodec : MessageToMessageCodec<ByteBuf, Any>() {
	companion object {
		val objectMapper = ObjectMapper()
	}

	override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
		val pojo = objectMapper.readValue<Any>(msg.toString(Charsets.UTF_8), Any::class.java)
		out += pojo
	}

	override fun encode(ctx: ChannelHandlerContext, msg: Any, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val bytes = objectMapper.writeValueAsBytes(msg)
		val byteBuf = ctx.alloc().buffer(bytes.size).writeBytes(bytes)
		out += byteBuf
	}

}
