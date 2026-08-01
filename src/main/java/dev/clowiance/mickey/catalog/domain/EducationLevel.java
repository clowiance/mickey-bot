package dev.clowiance.mickey.catalog.domain;

public enum EducationLevel {
    BACHELOR("Бакалаврат"),
    MASTER("Магістратура");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EducationLevel fromSlug(String value) {
        return "master".equalsIgnoreCase(value) ? MASTER : BACHELOR;
    }
}
