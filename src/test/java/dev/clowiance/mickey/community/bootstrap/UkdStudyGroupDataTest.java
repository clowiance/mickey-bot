package dev.clowiance.mickey.community.bootstrap;

import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UkdStudyGroupDataTest {

    @Test
    void keepsEveryGroupFromTheCuratorTable() {
        List<StudyGroup> entries = UkdStudyGroupData.entries();

        assertThat(entries).hasSize(19);
        assertThat(countOf(StudyGroupCategory.UNIVERSITY)).isEqualTo(7);
        assertThat(countOf(StudyGroupCategory.COLLEGE)).isEqualTo(11);
        assertThat(countOf(StudyGroupCategory.UNSPECIFIED)).isEqualTo(1);
    }

    @Test
    void pointsEveryGroupToItsOwnTelegramInvite() {
        List<StudyGroup> entries = UkdStudyGroupData.entries();

        assertThat(entries).allSatisfy(group ->
                assertThat(group.inviteLink()).startsWith("https://t.me/+"));
        assertThat(entries.stream().map(StudyGroup::inviteLink).collect(Collectors.toSet()))
                .as("однакове посилання у двох груп означало б помилку переносу")
                .hasSize(entries.size());
    }

    @Test
    void allowsSameSpecialtyInDifferentCoursesAndCategories() {
        List<StudyGroup> marketing = UkdStudyGroupData.entries().stream()
                .filter(group -> group.specialty().equals("Маркетинг"))
                .toList();

        assertThat(marketing).hasSize(3);
        assertThat(marketing).extracting(StudyGroup::course)
                .containsExactlyInAnyOrder("1 курс", "2 курс", "Після 9 класу");
    }

    @Test
    void namesEverySpecialtyAndLeavesUnknownCourseEmpty() {
        assertThat(UkdStudyGroupData.entries()).allSatisfy(group -> {
            assertThat(group.specialty()).isNotBlank();
            assertThat(group.category()).isNotNull();
            if (!group.hasCourse()) {
                assertThat(group.course()).isNull();
            }
        });
    }

    private long countOf(StudyGroupCategory category) {
        return UkdStudyGroupData.entries().stream()
                .filter(group -> group.category() == category)
                .count();
    }
}
