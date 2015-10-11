ALTER TABLE users RENAME COLUMN facebookid TO loginid;
ALTER TABLE users ADD COLUMN logintype VARCHAR(30);
