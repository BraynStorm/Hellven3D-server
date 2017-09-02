--Retrieve character ids,races, names.
SELECT
	characters.id AS char_id,
	characters.name,
	characters.race
FROM characters
	LEFT OUTER JOIN accounts ON characters.account_id = accounts.id
WHERE accounts.id = ? AND characters.world_id = ?;
