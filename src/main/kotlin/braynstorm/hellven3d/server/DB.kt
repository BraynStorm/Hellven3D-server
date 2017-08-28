package braynstorm.hellven3d.server

import POJO
import com.mchange.v2.c3p0.ComboPooledDataSource

object DB {

	private val connectionPool = ComboPooledDataSource(true).also {
		it.dataSourceName = "HellvenMainDB"
		it.driverClass = "org.postgresql.Driver"
		it.jdbcUrl = "jdbc:postgresql://localhost:5432/hellven3d"

		it.user = "server_user"
		it.password = "server_password"

		it.minPoolSize = 0
		it.maxPoolSize = 25

		// Enable statement pooling.
		it.maxStatementsPerConnection = 10

	}

	private inline fun toHostname(ip: String, port: Int) = "$ip:$port"


	/**
	 * Returns the account if any
	 */
	fun tryGetAccount(email: String, password: String): Pair<Account?, Boolean> {
		connectionPool.connection.use {
			val stmt = it.prepareStatement("SELECT id, logged_in FROM accounts WHERE email=? AND password = crypt(?, password)")
			stmt.setString(1, email)
			stmt.setString(2, password)

			stmt.executeQuery().use {
				return if (it.first()) {
					val id = it.getInt(1)
					val isAlreadyLoggedIn = it.getBoolean(2)

					Account(id) to isAlreadyLoggedIn

				} else {
					(null to false)
				}
			}
		}
	}


	// INTERNALS
	fun internalGetItemList(): List<Triple<Int, Short, Short>> {
		connectionPool.connection.use {
			it.createStatement().executeQuery("SELECT * FROM items").use {
				val list = mutableListOf<Triple<Int, Short, Short>>()

				it.forEach {
					list += Triple(it.getInt(1), it.getShort(2), it.getShort(3))
				}

				if (list.isEmpty()) {
					// TODO Log [DEBUG] Database has no items.

					println("[Database] table `items` is empty.  :/")
				} else {
					println("[Database] Loaded ${list.size} items ")
				}

				return list
			}
		}
	}

	fun getWorldList(): List<POJO.WorldInfo> {
		connectionPool.connection.use {
			val statement = it.prepareStatement("""SELECT
  worlds.id,
  worlds.name,
  world_servers.connection_string,
  world_servers.clients_max,
  world_servers.clients_current,
  count(characters.id) AS characters
FROM characters
  RIGHT OUTER JOIN worlds ON characters.world_id = worlds.id
  LEFT OUTER JOIN world_servers ON worlds.id = world_servers.world_id
GROUP BY worlds.id, world_servers.connection_string, world_servers.clients_max, world_servers.clients_current""")
			statement.use {
				it.executeQuery().use {
					TODO()

				}
			}

		}
	}

	/**
	 * TODO A lot of things left to think about; Read below.
	 * The JBDC doesnt allow for multithreaded prepared statements.
	 * java.sql.DataSource is a "connection pool" that should be used by the login server because of multithreaded access (async socekts).
	 *
	 * Which means the world server will be as planned - keeps all the necessary stuff for the game in memory.
	 * And every so often (probably gradually) the worldserver saves the progress of each player and "thing" in the game.
	 * Which also means the WorldServer CAN maintain a constant connection to the DB as long as it uses it in a
	 * one-thread-at-a-time fasion.
	 *
	 * By proxy of this "connection pool", the LoginServer will be unable to use prepared statements and will have to
	 * use the "older", normal statements. (SQL Injection prone ones...)
	 *
	 * Bojidar Borislavov Stoyanov (bojidar.b.stoyanov@gmail.com)
	 *
	 * 24.08.2017
	 */

	//	private object Statements {
//		// TODO Move the ones required by the LoginServer in a separate class
//
//		// Internal
//		val INTERNAL_ITEMLIST = dbConnection.prepareStatement("SELECT * FROM items")
//
//		val REGISTER_WORLD_SERVER = dbConnection.prepareCall(
//				"INSERT INTO world_servers (connection_string, clients_max, clients_current, world_id) VALUES (?, ?, ?, ?)"
//		)
//
//		val UNREGISTER_WORLD_SERVER = dbConnection.prepareCall(
//				"DELETE FROM world_servers WHERE connection_string=?"
//		)
//
//		val UPDATE_WORLD_SERVER = dbConnection.prepareCall(
//				"UPDATE world_servers SET clients_current=? WHERE connection_string=?"
//		)
//
//		val CREDENTIALS_REGISTER = dbConnection.prepareCall(
//				"INSERT INTO accounts (email, password) VALUES (?, crypt(?, gen_salt('bf', 10)));"
//		)
//
//		val CREDENTIALS_CHECK = dbConnection.prepareStatement(
//				"SELECT id FROM accounts WHERE email=? AND password = crypt(?, password)"
//		)
//		val ACCOUNT_GET_CHARACTERS = dbConnection.prepareStatement(
//				"SELECT id,name,race FROM characters WHERE account_id=?"
//		)
//
//		val GET_CHARACTER_PROTOTYPE = dbConnection.prepareStatement(
//				"SELECT item_id, equipment_slot FROM equipment WHERE character_id=?"
//		)
//
//		val REQUEST_SERVER_STATUS = dbConnection.prepareStatement(
//				"SELECT world_servers.clients_max, world_servers.clients_current FROM world_servers JOIN worlds ON world_servers.world_id = worlds.id WHERE worlds.name=?"
//		)
//		val REQUEST_SERVER_STATUS_ALL = dbConnection.prepareStatement(
//				"SELECT worlds.name, world_servers.clients_max, world_servers.clients_current FROM world_servers JOIN worlds ON world_servers.world_id = worlds.id WHERE worlds.name=?"
//		)
//
//	}


//	fun regiserWorldServer(ip: String, port: Int, clientsMax: Int, clientsCurrent: Int, worldID: Int): Int {
//		Statements.REGISTER_WORLD_SERVER.setString(1, connectionString(ip, port))
//		Statements.REGISTER_WORLD_SERVER.setInt(2, clientsMax)
//		Statements.REGISTER_WORLD_SERVER.setInt(3, clientsCurrent)
//		Statements.REGISTER_WORLD_SERVER.setInt(4, worldID)
//
//		return Statements.REGISTER_WORLD_SERVER.executeUpdate()
//	}
//
//	fun unregisterWorldServer(ip: String, port: Int): Int {
//		Statements.UNREGISTER_WORLD_SERVER.setString(1, connectionString(ip, port))
//		return Statements.UNREGISTER_WORLD_SERVER.executeUpdate()
//	}
//
//	fun updateWorldServer(ip: String, port: Int, clientsCurrent: Int): Int {
//		Statements.UPDATE_WORLD_SERVER.setInt(1, clientsCurrent)
//		Statements.UPDATE_WORLD_SERVER.setString(2, connectionString(ip, port))
//
//		org.jcp
//
//		return Statements.UNREGISTER_WORLD_SERVER.executeUpdate()
//	}
//
//
//	/**
//	 * @return pair in format: MaxCapacity, Current
//	 */
//	@Throws(UnknownWorld::class)
//	fun requestServerStatus(worldName: String): Pair<Int, Int> {
//		Statements.REQUEST_SERVER_STATUS.setString(1, worldName)
//		val resultSet = Statements.REQUEST_SERVER_STATUS.executeQuery()
//
//		if (!resultSet.first()) {
//			throw UnknownWorld(worldName)
//		}
//
//		return resultSet.getInt(1) to resultSet.getInt(2)
//	}
//
//	/**
//	 * @return set with triples in format: Name, MaxCapacity, Current
//	 */
//	fun requestServerStatusAll(): Set<Triple<String, Int, Int>> {
//
//		val servers = mutableSetOf<Triple<String, Int, Int>>()
//
//		Statements.REQUEST_SERVER_STATUS_ALL.executeQuery().forEach {
//			servers += Triple(it.getString(1), it.getInt(2), it.getInt(3))
//		}
//
//		return servers
//	}
//
//	/**
//	 * @return List<Pair<Name, RaceID>>
//	 */
//	fun getCharacterList(accountID: Int): List<Pair<String, Int>> {
//		Statements.ACCOUNT_GET_CHARACTERS.setInt(1, accountID)
//
//		val list = mutableListOf<Pair<String, Int>>()
//
//		Statements.ACCOUNT_GET_CHARACTERS.executeQuery().forEach {
//			list += it.getString(2) to it.getInt(3)
//		}
//
//		return list
//	}
//
//	fun getCharacterPrototypeList(accountID: Int): List<POJO.CharacterPrototype> {
//
//		return getCharacterList(accountID).map {
//
//		}
//
//	}
//
//	fun checkCredentials(email: String, password: String): Account? {
//		Statements.CREDENTIALS_CHECK.setString(1, email)
//		Statements.CREDENTIALS_CHECK.setString(2, password)
//
//		val resultSet = Statements.CREDENTIALS_CHECK.executeQuery()
//
//		// If we have a matching row, we found an account.
//		if (resultSet.first()) {
//			val accID = resultSet.getInt(1)
//
//			return Account(accID)
//
//			// TODO make the authenticated connection
//		}
//	}
//
//


}





