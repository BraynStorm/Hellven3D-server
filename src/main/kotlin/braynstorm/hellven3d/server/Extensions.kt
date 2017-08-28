package braynstorm.hellven3d.server

import java.sql.ResultSet


inline fun ResultSet.forEach(action: (arg: ResultSet) -> Unit) {
	while (next()) {
		action(this)
	}
}


