package hellven3d.server.launcher

import hellven3d.server.world.WorldServer

fun main(args: Array<String>) {
	WorldServer(
			clientPort = 6502,
			loginServerAddress = "127.0.0.1",
			loginServerPort = 6499
	).start()
}
