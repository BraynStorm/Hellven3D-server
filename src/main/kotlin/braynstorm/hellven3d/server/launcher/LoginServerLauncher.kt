package braynstorm.hellven3d.server.launcher

import braynstorm.hellven3d.server.LoginServer
import org.apache.log4j.Level
import org.apache.log4j.Logger


fun main(args: Array<String>) {
	Logger.getLogger("com.mchange").level = Level.INFO
	Logger.getLogger("io.netty").level = Level.INFO
	LoginServer(6500, 6502).start()
}
