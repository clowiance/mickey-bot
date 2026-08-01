package dev.clowiance.mickey.catalog.domain;

public enum TuitionPeriod {
    FIRST_YEAR("І курс"),
    EIGHTEEN_MONTH_PROGRAM("1,5 року");

    private final String displayName;

    TuitionPeriod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
