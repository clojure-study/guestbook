CREATE TABLE users
(user_id SERIAL PRIMARY KEY,
 name VARCHAR(60),
 password VARCHAR(200),
 timestamp TIMESTAMP);
--
-- ALTER TABLE guestbook add user_id INTEGER
