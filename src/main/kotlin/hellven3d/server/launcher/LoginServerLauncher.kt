package hellven3d.server.launcher

import hellven3d.server.auth.LoginServer


fun main(args: Array<String>) {
	LoginServer(6500, 6502).start()
}
