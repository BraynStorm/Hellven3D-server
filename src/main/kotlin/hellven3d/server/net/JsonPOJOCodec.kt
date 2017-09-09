package hellven3d.server.net

import com.owlike.genson.GensonBuilder
import hellven3d.net.ExternalPOJO
import hellven3d.net.POJO
import hellven3d.server.lazyLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder

internal val genson = GensonBuilder()
		.useConstructorWithArguments(true)
		.useClassMetadata(true)
		.useClassMetadataWithStaticType(true)
		.create()

class JsonPOJOEncoder : MessageToMessageEncoder<ExternalPOJO>() {
	companion object {
		val logger by lazyLogger()
	}

	override fun encode(ctx: ChannelHandlerContext, msg: ExternalPOJO, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		try {
			val bytes = genson.serializeBytes(msg)
			logger.trace("Encoded ${genson.serialize(msg)}")
			val byteBuf = ctx.alloc().buffer(bytes.size).writeBytes(bytes)
			out += byteBuf
		} catch (e: Exception) {
			logger.error("Exception during encoding of packet.", e)
		}
	}
}

class JsonPOJODecoder<T : POJO>(private val pojoClass: Class<T>) : MessageToMessageDecoder<ByteBuf>() {
	companion object {
		val logger by lazyLogger()
	}

	override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
		// By specification, Charsets.UTF_8 is the charset
		val remoteAddress = ctx.channel().remoteAddress().toString()
		val jsonString = msg.toString(Charsets.UTF_8)
		try {
			val pojo = genson.deserialize(jsonString, pojoClass)
			if (pojo != null) {
				if (pojo is ExternalPOJO) {
					out.add(pojo)
				} else {
					val packetData = ByteBufUtil.prettyHexDump(msg)
					logger.warn("[$remoteAddress] Received a non-pojo from client. Punish!\n$packetData\n")
					ctx.close()
					// TODO PUNISH
				}
			}

		} catch (e: Exception) {
			val packetData = ByteBufUtil.prettyHexDump(msg)
			logger.warn("[$remoteAddress] Exception when parsing json from client.\n$packetData\n ", e)
			ctx.close()
			//TODO PUNISH
		}

	}


}

