package hellven3d.server

class AccountNotFound(email: String) : Exception(email)
class UnknownWorld(worldName: String) : Exception(worldName)
