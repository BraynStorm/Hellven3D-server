package hellven3d.net

import com.owlike.genson.annotation.JsonProperty

interface InternalPOJO : POJO

data class RegisterWorldServer(
		@JsonProperty("connectionString") val connectionString: String,
		@JsonProperty("capacity") val capacity: Int,
		@JsonProperty("current") val current: Int
) : InternalPOJO
