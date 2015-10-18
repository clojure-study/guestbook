ALTER TABLE users DROP CONSTRAINT unique_name;
ALTER TABLE guestbook ADD COLUMN user_id integer;
UPDATE guestbook AS g
SET user_id = u.user_id
FROM users AS u
WHERE g.name = u.name;
ALTER TABLE guestbook DROP COLUMN name;
ALTER TABLE guestbook ADD FOREIGN KEY (user_id) REFERENCES users(user_id);
