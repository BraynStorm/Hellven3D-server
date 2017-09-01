--Retrieve character ids,races, names.
SELECT
	characters.id AS char_id,
	characters.name,
	characters.race
FROM worlds
	LEFT OUTER JOIN characters ON worlds.id = characters.world_id
	LEFT OUTER JOIN accounts ON characters.account_id = accounts.id
WHERE accounts.id = ? AND worlds.name = ?;
