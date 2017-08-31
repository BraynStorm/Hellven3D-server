package hellven3d.server

import com.mchange.v1.io.InputStreamUtils
import org.junit.jupiter.api.Test

// Dev Tests
class EzTest {

	@Test
	fun test() {
		InputStreamUtils.getContentsAsString(javaClass.classLoader.getResourceAsStream("sql")).split('\n').forEach {
			println(javaClass.classLoader.getResource("sql/" + it).readText(Charsets.UTF_8))
		}

	}

}
