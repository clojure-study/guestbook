--name:save-message!
-- creates a new message
INSERT INTO guestbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

--name:get-messages
-- selects all available messages
SELECT * from guestbook ORDER BY TIMESTAMP desc

--name:delete-message!
-- delete messages
DELETE from guestbook
WHERE id::varchar = :id

--name:get-message
-- select message
SELECT * from guestbook
WHERE id::varchar = :id
ORDER BY TIMESTAMP desc

--name:update-message!
-- update message
UPDATE guestbook
SET message = :message
WHERE id::varchar = :id

--name:save-user!
-- creates a new user
INSERT INTO users (name, facebookid,password, timestamp) VALUES (:name,:facebookid, :password, :timestamp)

--name:check-user-exists
SELECT * from users
WHERE name = :name

--name:get-facebook-user
SELECT * from users
WHERE facebookid = :facebookid

--name:signin-user
-- select user with name and password
SELECT * from users
WHERE name = :name AND password = :password

--name:get-names
-- get names
SELECT name from users
