-- Updates the logged_in flag on a given account.
UPDATE accounts
SET logged_in = ?
WHERE id =?
