package dev.clowiance.mickey.community.domain;

public enum StudyGroupCategory {

    UNIVERSITY("🎓", "Університет (УНІК)"),
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
}
