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


	fun getCharacterPrototypeList(accountID: Int, world: String): POJO.CharacterPrototypeList {
		logger.trace("Getting connection")

		connectionPool.connection.use {
			logger.trace("Preparing statement: GetCharacterPrototypeList")

			var stmt = it.prepareStatement(statements["GetCharacterPrototypeList"])

			stmt.setInt(1, accountID)
			stmt.setString(2, world)

			val nakedCharacters = hashMapOf<Int, POJO.CharacterPrototype>()

			logger.trace("Executing statement")
			stmt.executeQuery().use {
				it.forEach {
					val charID = it.getInt(1)
					val name = it.getString(2)
					val race = it.getInt(3)

					val nakedCharacter = POJO.CharacterPrototype().also {
						it.name = name
						it.race = race
						it.equipment = mutableListOf()
					}

					logger.trace("Got naked character $nakedCharacter")

					nakedCharacters += charID to nakedCharacter
				}
			}

			logger.trace("Preparing statement: GetCharacterPrototypeEquipment")
			stmt = it.prepareStatement(statements["GetCharacterPrototypeEquipment"])

			return POJO.CharacterPrototypeList().also {
				it.characters = nakedCharacters.map { nakedChar ->
					logger.trace("Equipping character $nakedChar")
					stmt.setInt(1, nakedChar.key)

					logger.trace("Executing statement")
					stmt.executeQuery().use {
						it.forEach {
							nakedChar.value.equipment.add(POJO.EquippedItem().also { item ->
								item.id = it.getInt(1)
								item.equipmentSlot = it.getInt(2)
								item.itemData = it.getString(3)
							})
						}
					}

					// its an equipped character now (well, except if they are not on Moonglade US :D).
					nakedChar.value
				}
			}
		}
	}

}




