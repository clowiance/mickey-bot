package dev.clowiance.mickey.community.domain;

/**
 * Чат однієї навчальної групи.
 *
 * @param course курс або база вступу («1 курс», «Після 9 класу»); null, якщо не вказано
 * @param note   службова примітка зі списку; користувачам не показується
 */
public record StudyGroup(
        StudyGroupCategory category,
        String specialty,
        String course,
        String inviteLink,
        String note
) {

    public boolean hasCourse() {
        return course != null && !course.isBlank();
    }
}
