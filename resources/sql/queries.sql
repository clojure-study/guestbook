--name:save-message!
-- creates a new message
INSERT INTO guestbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

--name:get-messages
-- selects all available messages
SELECT * from guestbook

--name:delete-message!
-- delete messages
DELETE from guestbook
WHERE id = :id;

--name:get-message
-- select message
SELECT * from guestbook
WHERE id = :id

--name:update-message!
-- update message
UPDATE guestbook
SET message = :message
WHERE id = :id;
