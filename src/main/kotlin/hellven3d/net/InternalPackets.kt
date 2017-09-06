package hellven3d.net

import com.owlike.genson.annotation.JsonProperty

interface InternalPOJO : POJO

data class RegisterWorldServer(
		@JsonProperty("connectionString") val connectionString: String,
		@JsonProperty("capacity") val capacity: Int,
		@JsonProperty("current") val current: Int
) : InternalPOJO

data class IsAccountPlaying(
		@JsonProperty("accountID") val accountID: Int
) : InternalPOJO

data class ResponseAccountPlaying(
		@JsonProperty("response") val response: Boolean
) : InternalPOJO

data class KickAccount(
		@JsonProperty("accountID") val accountID: Int,
		@JsonProperty("reason") val reason: Int
) : InternalPOJO {
	companion object {
		val REASON_UNKNOWN = 0
		val REASON_ACCOUNT_LOGGED_IN = 1
	}
}



