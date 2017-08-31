package hellven3d.server.launcher

import hellven3d.server.LoginServer


fun main(args: Array<String>) {
	LoginServer(6500, 6502).start()
}
