import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class InternalPOJO implements JsonPOJO {
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RegisterWorldServer extends InternalPOJO {
		public String connectionString;
		public int worldID;
		public int capacity;
		public int current;
	}
	
	
}
