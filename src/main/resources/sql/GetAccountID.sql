-- Retrieves the account ID.
SELECT
	id,
	logged_in
FROM accounts
WHERE email = ? AND password = crypt(?, password)
