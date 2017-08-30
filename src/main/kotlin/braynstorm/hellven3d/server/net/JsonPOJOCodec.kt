package braynstorm.hellven3d.server.net

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder

class JsonPOJOEncoder<T>(private val pojoClass: Class<T>) : MessageToMessageEncoder<T>() {
	companion object {
		val objectMapper = ObjectMapper()
	}

	override fun encode(ctx: ChannelHandlerContext, msg: T, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val bytes = objectMapper.writeValueAsBytes(msg)
		val byteBuf = ctx.alloc().buffer(bytes.size).writeBytes(bytes)
		out += byteBuf
	}
}

class JsonPOJODecoder<T>(private val pojoClass: Class<T>) : MessageToMessageDecoder<ByteBuf>() {
	companion object {
		val objectMapper = ObjectMapper()
	}

	override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val jsonString = msg.toString(Charsets.UTF_8)
		try {
			val pojo = objectMapper.readValue(jsonString, pojoClass)

			if (pojo != null)
				out.add(pojo)

		} catch (e: Exception) {
			e.printStackTrace()
			//TODO LOG KILL PUNISH
		}

	}


}

