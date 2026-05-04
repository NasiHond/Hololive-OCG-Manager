package com.fhict.hololiveocgmanager.domain;

/**
 * Enum representing visibility levels for collections and decks.
 * Supports three visibility states:
 * - PUBLIC: Visible to all users
 * - PRIVATE: Visible only to the owner
 * - UNLISTED: Visible via direct link but not searchable
 */
public enum Visibility {
    PUBLIC("Public", 1),
    PRIVATE("Private", 2),
    UNLISTED("Unlisted", 3);

    private final String displayName;
    private final int legacyValue;

    Visibility(String displayName, int legacyValue) {
        this.displayName = displayName;
        this.legacyValue = legacyValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLegacyValue() {
        return legacyValue;
    }

    /**
     * Convert legacy integer values to enum for backward compatibility
     */
    public static Visibility fromLegacyValue(Integer value) {
        if (value == null) {
            return PRIVATE;
        }
        for (Visibility visibility : Visibility.values()) {
            if (visibility.legacyValue == value) {
                return visibility;
            }
        }
        return PRIVATE; // Default to private if unknown
    }
}

