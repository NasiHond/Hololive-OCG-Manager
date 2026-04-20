ALTER TABLE users
    ADD profile_image_url VARCHAR(255);

ALTER TABLE cardtypes
    ALTER COLUMN name SET DEFAULT null;