SELECT
	worlds.id                  AS world_id,
	worlds.name                AS world_name,
	world_servers.clients_current,
	world_servers.clients_max,
	count(characters.world_id) AS character_count,
	world_servers.connection_string
FROM worlds
	LEFT OUTER JOIN characters ON (characters.world_id = worlds.id AND account_id = 3)
	LEFT OUTER JOIN world_servers ON worlds.id = world_servers.world_id
GROUP BY worlds.id,
	worlds.name,
	world_servers.clients_current,
	world_servers.clients_max,
	world_servers.connection_string;
