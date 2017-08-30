import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class POJO {
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class LoginServerInfo extends POJO {
		public long millis;
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{millis=" + millis + ')';
		}
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class AuthStart extends POJO {
		
		public String email;
		public String password;
		
		@Override
		public String toString() {
			return getClass().getSimpleName() +
					"{email=" + email +
					", password(==null)=" + (password == null) +
					"}";
		}
		
	}
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class AuthFinished extends POJO {
		
		public static final int SUCCESS = 0;
		public static final int NO_ACCOUNT = 1;
		public static final int ACCOUNT_ALREADY_LOGGED_IN = 2;
		public int status;
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{status=" + status + "}";
		}
	}
	
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RequestWorldList extends POJO {
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{}";
		}
	}
	
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class WorldList extends POJO {
		public List<WorldInfo> worlds;
		
		@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
		public static class WorldInfo extends POJO {
			public String name;
			public String connectionString;
			public int capacity;
			public int currentPlayers;
			public int numCharacters;
		}
	}
	
	
	/**
	 * "I Choose U"
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class RequestCharacterPrototypeList extends POJO {
		public String world;
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{world=" + world + "}";
		}
		
	}
	
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
	public static class CharacterPrototypeList extends POJO {
		public List<CharacterPrototype> characters;
		
		@Override
		public String toString() {
			return "CharacterPrototypeList{" +
					"characters(size)=" + characters.size() +
					'}';
		}
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
	public static class Token extends POJO {
		public long token;
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{token=" + token + "}";
		}
		
	}
	
}
