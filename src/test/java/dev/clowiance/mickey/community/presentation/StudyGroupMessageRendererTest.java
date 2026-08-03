package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.bootstrap.UkdStudyGroupData;
import dev.clowiance.mickey.community.domain.StudyGroup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyGroupMessageRendererTest {

    private static final int TELEGRAM_MESSAGE_LIMIT = 4096;

    private final StudyGroupMessageRenderer renderer = new StudyGroupMessageRenderer(new dev.clowiance.mickey.community.application.StudyGroupDirectory() {
        @Override public java.util.List<StudyGroup> findActive() { return UkdStudyGroupData.entries(); }
        @Override public java.util.List<StudyGroup> findAll() { return UkdStudyGroupData.entries(); }
        @Override public java.util.Optional<StudyGroup> findById(Long id) { return java.util.Optional.empty(); }
        @Override public java.util.Optional<StudyGroup> findByInviteLink(String link) { return java.util.Optional.empty(); }
        @Override public StudyGroup save(StudyGroup group) { return group; }
        @Override public StudyGroup setActive(Long id, boolean active) { throw new UnsupportedOperationException(); }
    });

    @Test
    void showsOnlyUniversityGroups() {
        String menu = renderer.renderAll();

        assertThat(menu).contains("ЧАТИ ГРУП УНІВЕРСИТЕТУ")
                .contains("8 груп")
                .contains("Логістика")
                .doesNotContain("УНІК")
                .doesNotContain("коледж");
    }

    @Test
    void turnsEveryGroupIntoClickableInvite() {
        String all = renderer.renderAll();

        for (StudyGroup group : UkdStudyGroupData.entries()) {
            assertThat(all)
                    .as("група %s має бути в списку з посиланням", group.specialty())
                    .contains("<a href=\"" + group.inviteLink() + "\">");
        }
    }

    @Test
    void keepsEveryViewWithinTelegramMessageLimit() {
        assertThat(renderer.renderAll().length()).isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
    }
}
