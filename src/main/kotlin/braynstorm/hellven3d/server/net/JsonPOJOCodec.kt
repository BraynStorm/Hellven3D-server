package braynstorm.hellven3d.server.net

import POJO
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder

class JsonPOJOEncoder : MessageToMessageEncoder<POJO>() {
	companion object {
		val objectMapper = ObjectMapper()
	}

	override fun encode(ctx: ChannelHandlerContext, msg: POJO, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val bytes = objectMapper.writeValueAsBytes(msg)
		val byteBuf = ctx.alloc().buffer(bytes.size).writeBytes(bytes)
		out += byteBuf
		println("ENCODE")
	}
}

class JsonPOJODecoder : MessageToMessageDecoder<ByteBuf>() {
	companion object {
		val objectMapper = ObjectMapper()
	}

	override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val bytes = objectMapper.writeValueAsBytes(msg)
		val byteBuf = ctx.alloc().buffer(bytes.size).writeBytes(bytes)
		out += byteBuf
		println("ENCODE")
	}


}

