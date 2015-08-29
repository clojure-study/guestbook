ALTER TABLE users ADD COLUMN facebookid VARCHAR(100);
CREATE INDEX facebookid ON users(facebookid);
