-- Retrieve items for a character.
SELECT
	item_id,
	equipment_slot,
	item_data
FROM equipment
WHERE character_id = ?;
