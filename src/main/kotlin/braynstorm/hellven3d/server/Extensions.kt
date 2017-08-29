package braynstorm.hellven3d.server

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

interface WithLoggin {
	val logger: Logger
}


inline fun ResultSet.forEach(action: (arg: ResultSet) -> Unit) {
	while (next()) {
		action(this)
	}
}

/**
 * Creates a logger for this class using SLF4J
 */
inline fun <reified T> T.lazyLogger(): Lazy<Logger> {
	return lazy { LoggerFactory.getLogger(T::class.java) }
}
