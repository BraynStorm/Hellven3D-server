import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class InternalPOJO {
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RegisterWorldServer {
		public String connectionString;
		public int worldID;
		public int capacity;
		public int current;
	}
	
	
}
