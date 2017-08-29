package braynstorm.hellven3d.server.launcher

import braynstorm.hellven3d.server.LoginServer


fun main(args: Array<String>) {
	LoginServer(6500, 6501).start()
}
