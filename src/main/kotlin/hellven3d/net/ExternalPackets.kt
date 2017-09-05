package hellven3d.net

import com.owlike.genson.annotation.JsonProperty

interface ExternalPOJO : POJO

data class LoginServerStatus(val millis: Long) : ExternalPOJO

data class AuthStart(
		@JsonProperty("email") val email: String,
		@JsonProperty("password") val password: String
) : ExternalPOJO

data class AuthFinished(
		val status: Int
) : ExternalPOJO {
	companion object {
		val SUCCESSFUL = 0
		val NO_SUCH_ACCOUNT = 1
		val ACCOUNT_ALREADY_LOGGED_IN = 2
	}
}


data class RequestCharacterList(
		@JsonProperty("worldID") val worldID: Int
) : ExternalPOJO

data class WorldList(
		val worlds: List<WorldInfo>
) : ExternalPOJO


class RequestWorldList : ExternalPOJO {

	override fun toString(): String {
		return javaClass.simpleName + "()"
	}
}

data class CharacterList(
		val list: List<CharacterPrototype>
) : ExternalPOJO

data class WorldInfo(
		val id: Int,
		val name: String,
		val capacity: Int,
		val current: Int,
		val yourCharacters: Int,
		val connectionString: String
)

data class CharacterPrototype(
		val id: Int,
		val name: String,
		val race: Int,
		val equipment: MutableMap<Int, Item>
)

data class Item(
		val id: Int,
		val type: Short,
		val subtype: Short
) {
	constructor(item: hellven3d.server.Item) : this(item.id, item.type, item.subtype)
}
