package hellven3d.server

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

interface WithLogging {
	val logger: Logger
}


inline fun ResultSet.forEach(action: (arg: ResultSet) -> Unit) {
	while (next()) {
		action(this)
	}
}

/**
 * Creates a logger for this class using SLF4J
 *
 * IntelliJ Note: (parameter is redundant) it isn't, see [DB.logger],
 * We don't need to specify explicitly what the type parameter is.
 *
 * Basically, it reduces this:
 *
 * ```val logger = lazyLogger<CLASS_NAME_HERE>();```
 *
 * to this
 *
 * ```val logger = lazyLogger();```
 */
inline fun <reified T> T.lazyLogger(): Lazy<Logger> {
	return lazy { LoggerFactory.getLogger(T::class.java) }
}
