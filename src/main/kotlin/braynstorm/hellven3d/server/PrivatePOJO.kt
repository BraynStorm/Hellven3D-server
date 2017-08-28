package braynstorm.hellven3d.server

import com.esotericsoftware.kryo.Kryo

sealed class PrivatePOJO {
	class RequestWorldServerInfo {
		var worldID = 0
		var totalCapacity = 0
		var currentCapacity = 0

	}

	class ClientConnectionLost {
		var accountID = 0
	}

	class ClientChoseYou {
		var accountID = 0
		var token = 0L
	}

	companion object {

		fun register(kryo: Kryo) {
			PrivatePOJO::class.nestedClasses.sortedBy { it.qualifiedName }.forEach {
				register(kryo, it.java)
			}
		}

		private inline fun register(kryo: Kryo, c: Class<*>) {

			// TODO Log [INFO] [Kryo] Registered class C
		}

	}
}
