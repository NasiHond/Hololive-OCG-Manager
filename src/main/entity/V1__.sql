ALTER TABLE arts
    ADD cost TEXT;

ALTER TABLE arts
    ADD effect TEXT;

ALTER TABLE arts
    DROP COLUMN name;

ALTER TABLE arts
    ADD name TEXT;