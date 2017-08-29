import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public class POJO {
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class LoginServerInfo extends POJO {
		public long millis;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RequestWorldList extends POJO {
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldServerStatusRequest extends POJO {
		/**
		 * May be equal to "*"
		 */
		public String world;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldServerStatusResponse extends POJO {
		public int capacity;
		public int loggedIn;
		public int inGame;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class AuthStart extends POJO {
		public String email;
		public String password;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class AuthFinished extends POJO {
		public static final int SUCCESS = 0;
		public static final int NO_ACCOUNT = 1;
		public static final int ACCOUNT_ALREADY_LOGGED_IN = 2;
		
		public int status;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class Token extends POJO {
		public long token;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RequestCharacterPrototypeList extends POJO {
		public String world;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class CharacterPrototypeList extends POJO {
		public List<CharacterPrototype> characters;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class CharacterPrototype extends POJO {
		public String name;
		public int race;
		public List<EquippedItem> equipment;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class EquippedItem extends POJO {
		public int id;
		// TODO more shit to add.
		// HashMap<String, Integer> data;
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldInfo extends POJO {
		public String name;
		public String connectionString;
		public int capacity;
		public int currentPlayers;
		public int numCharacters;
	}
	
}
