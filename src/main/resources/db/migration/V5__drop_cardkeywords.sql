ALTER TABLE public.cardkeywords
    DROP CONSTRAINT fk_cardkeywords_on_cardid;

ALTER TABLE public.cardkeywords
    DROP CONSTRAINT fk_cardkeywords_on_keywordid;

ALTER TABLE keywords
    ADD card_id INTEGER;

ALTER TABLE keywords
    ALTER COLUMN card_id SET NOT NULL;

ALTER TABLE keywords
    ADD CONSTRAINT FK_KEYWORDS_ON_CARD FOREIGN KEY (card_id) REFERENCES cards (id) ON DELETE CASCADE;

DROP TABLE public.cardkeywords CASCADE;

ALTER TABLE cardtypes
    ALTER COLUMN name SET DEFAULT null;