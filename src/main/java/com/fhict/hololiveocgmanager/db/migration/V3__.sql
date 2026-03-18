ALTER TABLE public.cards
    DROP CONSTRAINT cards_cardtypes_name_fk;

ALTER TABLE arts
    ADD crit_colour_id INTEGER;

ALTER TABLE arts
    ADD CONSTRAINT FK_ARTS_ON_CRIT_COLOUR FOREIGN KEY (crit_colour_id) REFERENCES public.colours (id);

ALTER TABLE public.arts
    DROP COLUMN cost;

DROP SEQUENCE public.colours_id_seq CASCADE;

ALTER TABLE cards
    DROP COLUMN batonpass;

ALTER TABLE cards
    DROP COLUMN bloomlvl;

ALTER TABLE cards
    DROP COLUMN cardid;

ALTER TABLE cards
    DROP COLUMN cardset;

ALTER TABLE cards
    DROP COLUMN cardtype;

ALTER TABLE cards
    DROP COLUMN holomem;

ALTER TABLE cards
    DROP COLUMN image;

ALTER TABLE cards
    DROP COLUMN rarity;

ALTER TABLE cards
    ADD batonpass TEXT NOT NULL;

ALTER TABLE cards
    ADD bloomlvl TEXT NOT NULL;

ALTER TABLE cards
    ADD cardid VARCHAR(255) NOT NULL;

ALTER TABLE cards
    ADD cardset TEXT NOT NULL;

ALTER TABLE cards
    ADD cardtype INTEGER NOT NULL;

ALTER TABLE cards
    ADD CONSTRAINT FK_CARDS_ON_CARDTYPE FOREIGN KEY (cardtype) REFERENCES cardtypes (id);

ALTER TABLE keywords
    DROP COLUMN effect;

ALTER TABLE keywords
    DROP COLUMN name;

ALTER TABLE keywords
    DROP COLUMN type;

ALTER TABLE keywords
    ADD effect TEXT NOT NULL;

ALTER TABLE cards
    ADD holomem TEXT NOT NULL;

ALTER TABLE cards
    ADD image TEXT;

ALTER TABLE cardtypes
    DROP COLUMN name;

ALTER TABLE cardtypes
    ADD name TEXT DEFAULT null NOT NULL;

ALTER TABLE keywords
    ADD name TEXT NOT NULL;

ALTER TABLE tags
    DROP COLUMN name;

ALTER TABLE tags
    ADD name TEXT NOT NULL;

ALTER TABLE cards
    ADD rarity TEXT NOT NULL;

ALTER TABLE keywords
    ADD type TEXT NOT NULL;

ALTER TABLE public.colours
    DROP COLUMN colour;

ALTER TABLE public.colours
    DROP COLUMN image_url;

ALTER TABLE public.colours
    ADD colour TEXT;

ALTER TABLE public.extra
    DROP COLUMN effect;

ALTER TABLE public.extra
    ADD effect TEXT;

CREATE SEQUENCE IF NOT EXISTS public.colours_id_seq;
ALTER TABLE public.colours
    ALTER COLUMN id SET NOT NULL;
ALTER TABLE public.colours
    ALTER COLUMN id SET DEFAULT nextval('public.colours_id_seq');

ALTER SEQUENCE public.colours_id_seq OWNED BY public.colours.id;

ALTER TABLE public.colours
    ADD image_url TEXT;