package dev.clowiance.mickey.administration.domain;

public enum ChatAudience {
    APPLICANTS("Абітурієнти"),
    STUDENTS("Студенти"),
    MIXED("Змішана спільнота");

    private final String displayName;

    ChatAudience(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatAudience next() {
        return switch (this) {
            case APPLICANTS -> STUDENTS;
            case STUDENTS -> MIXED;
            case MIXED -> APPLICANTS;
        };
    }
}
