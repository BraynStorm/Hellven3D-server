package hellven3d.server

import POJO
import com.mchange.v1.io.InputStreamUtils
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("NOTHING_TO_INLINE")
object DB : WithLogging {
	override val logger by lazyLogger()

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

	private val statements = hashMapOf<String, String>().also { map ->
		// Regex removes all comments and strips the empty lines.
		val regex = """(?:--.*?\n)|(?:\n\n)|(?:^\n$)""".toRegex(setOf(RegexOption.MULTILINE))
		InputStreamUtils.getContentsAsString(javaClass.classLoader.getResourceAsStream("sql")).split('\n').forEach {
			val path = "sql/" + it
			if (it.isNotBlank()) {

				var sql = javaClass.classLoader.getResource(path).readText(Charsets.UTF_8)
				sql = regex.replace(sql, "")

				// Names will be (ex) GetWorldList
				val key = it.removeSuffix(".sql")
				map += key to sql
				logger.info("Loaded sql file $key")
			}
		}
	}

	private inline fun toConnectionString(ip: String, port: Int) = "$ip:$port"
	private inline fun toIpAndPort(connectionString: String): Pair<String, Int> {
		val split = connectionString.split(':')
		return split[0] to split[1].toInt()
	}

	private inline fun prepareStatement(connnection: Connection, statement: String): PreparedStatement {
		return connnection.prepareStatement(statement, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
	}

	/**
	 * Returns the account if any
	 */
	fun tryGetAccount(email: String, password: String): Pair<Account?, Boolean> {
		connectionPool.connection.use {
			val stmt = it.prepareStatement(statements["GetAccountID"])
			stmt.setString(1, email)
			stmt.setString(2, password)

			stmt.executeQuery().use {
				return if (!it.next()) {
					(null to false)
				} else {
					val id = it.getInt(1)
					val isAlreadyLoggedIn = it.getBoolean(2)

					Account(id) to isAlreadyLoggedIn
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
					val t = Triple(it.getInt(1), it.getShort(2), it.getShort(3))
					list += t
					logger.debug("DB returned item $t")
				}

				if (list.isEmpty()) {
					logger.error("Database contains no items.")
				} else {
					logger.info("Loaded ${list.size} items.")
				}

				return list
			}
		}
	}

	fun getWorldListAndCharacterCount(accountID: Int): POJO.WorldList {
		connectionPool.connection.use {
			it.prepareStatement(statements["GetWorldList"]).use { statement ->
				statement.setInt(1, accountID)
				statement.executeQuery().use { resultSet ->
					val worldList = POJO.WorldList().also { it.worlds = mutableListOf<POJO.WorldList.WorldInfo>() }

					resultSet.forEach { row ->
						worldList.worlds.add(POJO.WorldList.WorldInfo().also {
							it.name = row.getString(1)
							it.currentPlayers = row.getInt(2)
							it.capacity = row.getInt(3)
							it.numCharacters = row.getInt(4)
							it.connectionString = row.getString(5)
						})
					}

					logger.trace("WorldList and character count for account $accountID gathered.")
					return worldList
				}
			}

		}

	}

	fun setAccountLoggedIn(accountID: Int, value: Boolean) {
		logger.trace("Account $accountID is already logged in")

		connectionPool.connection.use {
			val stmt = it.prepareStatement(statements["SetAccountLoggedIn"])
			stmt.setBoolean(1, value)
			stmt.setInt(2, accountID)

			stmt.executeUpdate()
		}
	}

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
//		if (!resultSet.next()) {
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
//		if (resultSet.next()) {
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




