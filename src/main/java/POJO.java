import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public class POJO {
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RequestWorldList {
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldServerStatusRequest {
		/**
		 * May be equal to "*"
		 */
		public String world;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldServerStatusResponse {
		public int capacity;
		public int loggedIn;
		public int inGame;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class AuthStart {
		public String email;
		public String password;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class AuthFinished {
		public static final int SUCCESS = 0;
		public static final int NO_ACCOUNT = 1;
		public static final int ACCOUNT_ALREADY_LOGGED_IN = 2;
		
		public int status;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class Token {
		public long token;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RequestCharacterPrototypeList {
		public String world;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class CharacterPrototypeList {
		public List<CharacterPrototype> characters;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class CharacterPrototype {
		public String name;
		public int race;
		public List<EquippedItem> equipment;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class EquippedItem {
		public int id;
		// TODO more shit to add.
		// HashMap<String, Integer> data;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldInfo {
		public String name;
		public String connectionString;
		public int numCharacters;
	}
	
}
