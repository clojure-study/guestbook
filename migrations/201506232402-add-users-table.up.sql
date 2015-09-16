CREATE TABLE guestbook
(id SERIAL PRIMARY KEY,
 name VARCHAR(30),
 message VARCHAR(200),
 timestamp TIMESTAMP);
