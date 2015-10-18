ALTER TABLE guestbook ADD COLUMN name VARCHAR(30);
UPDATE guestbook AS g
SET name = u.name
FROM users AS u
WHERE g.user_id = u.user_id;
ALTER TABLE guestbook DROP COLUMN user_id;
ALTER TABLE users ADD CONSTRAINT unique_name UNIQUE (name);
