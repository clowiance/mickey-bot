package dev.clowiance.mickey.catalog.domain;

public record Specialty(
        Long id,
        String catalogKey,
        String code,
        String name,
        String educationalProgram,
        EducationLevel educationLevel,
        Long fullTimeTuition,
        Long partTimeTuition,
        TuitionPeriod tuitionPeriod,
        String tuitionNote,
        String sourceUrl,
        int sourceYear,
        boolean active,
        int displayOrder
) {
    public Specialty {
        if (catalogKey == null || catalogKey.isBlank()) {
            throw new IllegalArgumentException("catalogKey must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (educationLevel == null || tuitionPeriod == null) {
            throw new IllegalArgumentException("educationLevel and tuitionPeriod are required");
        }
    }

    public Specialty withTuition(StudyForm form, Long amount) {
        return new Specialty(id, catalogKey, code, name, educationalProgram, educationLevel,
                form == StudyForm.FULL_TIME ? amount : fullTimeTuition,
                form == StudyForm.PART_TIME ? amount : partTimeTuition,
                tuitionPeriod, tuitionNote, sourceUrl, sourceYear, active, displayOrder);
    }

    public Specialty withActive(boolean newActive) {
        return new Specialty(id, catalogKey, code, name, educationalProgram, educationLevel,
                fullTimeTuition, partTimeTuition, tuitionPeriod, tuitionNote, sourceUrl,
                sourceYear, newActive, displayOrder);
    }
}
