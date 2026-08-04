package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.bootstrap.UkdStudyGroupData;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
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
    void menuCountsEveryCategorySeparately() {
        String menu = renderer.renderMenu();

        assertThat(menu).contains("ЧАТИ НАВЧАЛЬНИХ ГРУП")
                .contains("Університет — 8")
                .contains("Фаховий коледж — 11")
                .contains("Обери, де ти навчаєшся");
    }

    @Test
    void collegeViewShowsCollegeGroupsAndNotUniversityOnes() {
        String college = renderer.renderCategory(StudyGroupCategory.COLLEGE);

        assertThat(college).contains("ФАХОВИЙ КОЛЕДЖ")
                .contains("Хореографія")
                .contains("Юридичне діловодство та офісна справа")
                .contains("Після 9 класу")
                .doesNotContain("Логістика");
    }

    @Test
    void universityViewShowsUniversityGroupsOnly() {
        String university = renderer.renderCategory(StudyGroupCategory.UNIVERSITY);

        assertThat(university).contains("Логістика")
                .doesNotContain("Хореографія");
    }

    @Test
    void countsGroupsPerCategory() {
        assertThat(renderer.countOf(StudyGroupCategory.UNIVERSITY)).isEqualTo(8);
        assertThat(renderer.countOf(StudyGroupCategory.COLLEGE)).isEqualTo(11);
        assertThat(renderer.countOf(StudyGroupCategory.UNSPECIFIED)).isEqualTo(1);
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
        assertThat(renderer.renderMenu().length()).isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        assertThat(renderer.renderAll().length()).isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        for (StudyGroupCategory category : StudyGroupCategory.values()) {
            assertThat(renderer.renderCategory(category).length())
                    .as("розділ %s має вміщатися в одне повідомлення", category)
                    .isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        }
    }
}
