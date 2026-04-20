package com.fhict.hololiveocgmanager.specification;

import com.fhict.hololiveocgmanager.entity.CardEntity;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecification {

    private static String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * Filter by card name (case-insensitive partial match on cardid).
     */
    public static Specification<CardEntity> cardNameContains(String cardName) {
        final String filter = normalizeFilter(cardName);
        return (root, query, cb) -> filter == null ? null
                : cb.like(cb.lower(root.get("cardid")), "%" + filter.toLowerCase() + "%");
    }

    /**
     * Filter by bloom level (exact match).
     */
    public static Specification<CardEntity> bloomLevelEquals(String bloomLvl) {
        final String filter = normalizeFilter(bloomLvl);
        return (root, query, cb) -> filter == null ? null
                : cb.equal(cb.lower(root.get("bloomlvl")), filter.toLowerCase());
    }

    /**
     * Filter by colour (exact match).
     */
    public static Specification<CardEntity> colourEquals(String colour) {
        final String filter = normalizeFilter(colour);
        return (root, query, cb) -> filter == null ? null
                : cb.equal(cb.lower(root.get("cardcolour").get("colour")), filter.toLowerCase());
    }

    /**
     * Filter by card set (case-insensitive partial match).
     */
    public static Specification<CardEntity> cardSetContains(String cardSet) {
        final String filter = normalizeFilter(cardSet);
        return (root, query, cb) -> filter == null ? null
                : cb.like(cb.lower(root.get("cardset")), "%" + filter.toLowerCase() + "%");
    }

    /**
     * Filter by rarity (exact match).
     */
    public static Specification<CardEntity> rarityEquals(String rarity) {
        final String filter = normalizeFilter(rarity);
        return (root, query, cb) -> filter == null ? null
                : cb.equal(cb.lower(root.get("rarity")), filter.toLowerCase());
    }

    /**
     * Filter by card type (exact match).
     */
    public static Specification<CardEntity> cardTypeEquals(String cardTypeName) {
        final String filter = normalizeFilter(cardTypeName);
        return (root, query, cb) -> filter == null ? null
                : cb.equal(cb.lower(root.get("cardtype").get("name")), filter.toLowerCase());
    }

    /**
     * Filter by holomem (case-insensitive partial match).
     */
    public static Specification<CardEntity> holomemContains(String holomem) {
        final String filter = normalizeFilter(holomem);
        return (root, query, cb) -> filter == null ? null
                : cb.like(cb.lower(root.get("holomem")), "%" + filter.toLowerCase() + "%");
    }

    /**
     * Filter parallel cards.
     * - true: keep only cards whose cardId appears more than once.
     * - false: keep only the first row per cardId (lowest id).
     * - blank/null: ignore.
     */
    public static Specification<CardEntity> parallelEquals(String parallel) {
        final String filter = normalizeFilter(parallel);
        if (filter == null) {
            return null;
        }

        if ("true".equalsIgnoreCase(filter)) {
            return (root, query, cb) -> {
                var duplicates = query.subquery(String.class);
                var duplicateRoot = duplicates.from(CardEntity.class);
                duplicates.select(duplicateRoot.get("cardid"))
                        .groupBy(duplicateRoot.get("cardid"))
                        .having(cb.gt(cb.count(duplicateRoot), 1L));
                return root.get("cardid").in(duplicates);
            };
        }

        if ("false".equalsIgnoreCase(filter)) {
            return (root, query, cb) -> {
                var firstCardId = query.subquery(Integer.class);
                var firstCardRoot = firstCardId.from(CardEntity.class);
                firstCardId.select(cb.min(firstCardRoot.get("id")))
                        .where(cb.equal(firstCardRoot.get("cardid"), root.get("cardid")));
                return cb.equal(root.get("id"), firstCardId);
            };
        }

        return null;
    }

    /**
     * Combine all filters with AND logic (null filters are ignored).
     */
    public static Specification<CardEntity> withFilters(
            String cardName,
            String bloomLvl,
            String colour,
            String cardSet,
            String rarity,
            String cardType,
            String holomem,
            String parallel) {
        var spec = Specification
                .where(cardNameContains(cardName))
                .and(bloomLevelEquals(bloomLvl))
                .and(colourEquals(colour))
                .and(cardSetContains(cardSet))
                .and(rarityEquals(rarity))
                .and(cardTypeEquals(cardType))
                .and(holomemContains(holomem));

        var parallelSpec = parallelEquals(parallel);
        return parallelSpec == null ? spec : spec.and(parallelSpec);
    }
}
