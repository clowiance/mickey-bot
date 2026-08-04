package dev.clowiance.mickey.community.domain;

/**
 * Університет і фаховий коледж — різні набори людей із різними базами вступу,
 * тому чати розведені по категоріях: змішаний список означає заявки не в той чат.
 */
public enum StudyGroupCategory {

    UNIVERSITY("🎓", "Університет"),
    COLLEGE("🏫", "Фаховий коледж"),
    UNSPECIFIED("❔", "Категорію не зазначено");

    private final String emoji;
    private final String displayName;

    StudyGroupCategory(String emoji, String displayName) {
        this.emoji = emoji;
        this.displayName = displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String slug() {
        return name().toLowerCase();
    }

    /**
     * Категорія зі значення callback-кнопки. Невідоме значення не має валити обробку,
     * тому повертаємо {@code null} — викликач сам вирішує, що показати.
     */
    public static StudyGroupCategory fromSlug(String slug) {
        if (slug == null) {
            return null;
        }
        for (StudyGroupCategory category : values()) {
            if (category.slug().equals(slug)) {
                return category;
            }
        }
        return null;
    }
}
