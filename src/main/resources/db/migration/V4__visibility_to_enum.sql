ALTER TABLE cardtypes
    ALTER COLUMN name SET DEFAULT null;

ALTER TABLE collections
    ALTER COLUMN owner_id SET NOT NULL;

ALTER TABLE collections
    DROP COLUMN visibility;

ALTER TABLE collections
    ADD visibility VARCHAR(255);

ALTER TABLE decks
    DROP COLUMN visibility;

ALTER TABLE decks
    ADD visibility VARCHAR(255) NOT NULL;