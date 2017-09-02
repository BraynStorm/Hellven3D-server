-- This querry returns a table with:
-- 1. All of the worlds currently in the database (offline ones too)
-- 2. If a given worlds has a WorldServer serving it currently, the table has the connection string,
--          maximum connections and current connections.
-- 3. Foe each world, how many characters does this account have

SELECT
	worlds.id,
	worlds.name                AS world_name,
	world_servers.clients_current,
	world_servers.clients_max,
	count(characters.world_id) AS character_count,
	world_servers.connection_string
FROM worlds
	LEFT OUTER JOIN characters ON (characters.world_id = worlds.id AND account_id = ?)
	LEFT OUTER JOIN world_servers ON worlds.id = world_servers.world_id
GROUP BY
	worlds.id,
	worlds.name,
	world_servers.clients_current,
	world_servers.clients_max,
	world_servers.connection_string;
