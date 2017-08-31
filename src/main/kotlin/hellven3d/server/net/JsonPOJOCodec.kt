package hellven3d.server.net

import JsonPOJO
import com.fasterxml.jackson.databind.ObjectMapper
import hellven3d.server.lazyLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder

class JsonPOJOEncoder : MessageToMessageEncoder<JsonPOJO>() {
	companion object {
		val objectMapper = ObjectMapper()
	}

	override fun encode(ctx: ChannelHandlerContext, msg: JsonPOJO, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val bytes = objectMapper.writeValueAsBytes(msg)
		val byteBuf = ctx.alloc().buffer(bytes.size).writeBytes(bytes)
		out += byteBuf
	}
}

class JsonPOJODecoder<T : JsonPOJO>(private val pojoClass: Class<T>) : MessageToMessageDecoder<ByteBuf>() {
	companion object {
		val logger by lazyLogger()
		val objectMapper = ObjectMapper()
	}

	override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val jsonString = msg.toString(Charsets.UTF_8)
		try {
			val pojo = objectMapper.readValue(jsonString, pojoClass)
			if (pojo != null) {
				if (pojo is JsonPOJO) {
					out.add(pojo)
				} else {
					logger.warn("Received a non-pojo from client. Punish!")
					ctx.close()
					// TODO PUNISH
				}
			}

		} catch (e: Exception) {
			logger.warn("Exception when parsing json from client. ", e)
			ctx.close()
			//TODO PUNISH
		}

	}


}

