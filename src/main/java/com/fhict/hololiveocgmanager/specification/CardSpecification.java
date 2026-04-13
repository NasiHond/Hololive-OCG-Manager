package com.fhict.hololiveocgmanager.specification;

import com.fhict.hololiveocgmanager.entity.CardEntity;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecification {

    /**
     * Filter by card name (case-insensitive partial match on cardid).
     */
    public static Specification<CardEntity> cardNameContains(String cardName) {
        return (root, query, cb) -> cardName == null ? null
                : cb.like(cb.lower(root.get("cardid")), "%" + cardName.toLowerCase() + "%");
    }

    /**
     * Filter by bloom level (exact match).
     */
    public static Specification<CardEntity> bloomLevelEquals(String bloomLvl) {
        return (root, query, cb) -> bloomLvl == null ? null
                : cb.equal(cb.lower(root.get("bloomlvl")), bloomLvl.toLowerCase());
    }

    /**
     * Filter by colour (exact match).
     */
    public static Specification<CardEntity> colourEquals(String colour) {
        return (root, query, cb) -> colour == null ? null
                : cb.equal(cb.lower(root.get("cardcolour").get("colour")), colour.toLowerCase());
    }

    /**
     * Filter by card set (case-insensitive partial match).
     */
    public static Specification<CardEntity> cardSetContains(String cardSet) {
        return (root, query, cb) -> cardSet == null ? null
                : cb.like(cb.lower(root.get("cardset")), "%" + cardSet.toLowerCase() + "%");
    }

    /**
     * Filter by rarity (exact match).
     */
    public static Specification<CardEntity> rarityEquals(String rarity) {
        return (root, query, cb) -> rarity == null ? null
                : cb.equal(cb.lower(root.get("rarity")), rarity.toLowerCase());
    }

    /**
     * Filter by card type (exact match).
     */
    public static Specification<CardEntity> cardTypeEquals(String cardTypeName) {
        return (root, query, cb) -> cardTypeName == null ? null
                : cb.equal(cb.lower(root.get("cardtype").get("name")), cardTypeName.toLowerCase());
    }

    /**
     * Filter by holomem (case-insensitive partial match).
     */
    public static Specification<CardEntity> holomemContains(String holomem) {
        return (root, query, cb) -> holomem == null ? null
                : cb.like(cb.lower(root.get("holomem")), "%" + holomem.toLowerCase() + "%");
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
            String holomem) {
        return Specification
                .where(cardNameContains(cardName))
                .and(bloomLevelEquals(bloomLvl))
                .and(colourEquals(colour))
                .and(cardSetContains(cardSet))
                .and(rarityEquals(rarity))
                .and(cardTypeEquals(cardType))
                .and(holomemContains(holomem));
    }
}
