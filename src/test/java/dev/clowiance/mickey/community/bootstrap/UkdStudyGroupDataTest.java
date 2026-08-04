package dev.clowiance.mickey.community.bootstrap;

import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UkdStudyGroupDataTest {

    /**
     * Чати коледжу вже одного разу зникли зі списку разом із категорією {@code COLLEGE}
     * — цей тест не дає їм зникнути вдруге.
     */
    @Test
    void keepsUniversityAndCollegeGroupsSeparately() {
        List<StudyGroup> entries = UkdStudyGroupData.entries();

        assertThat(entries).hasSize(20);
        assertThat(countOf(StudyGroupCategory.UNIVERSITY)).isEqualTo(8);
        assertThat(countOf(StudyGroupCategory.COLLEGE)).isEqualTo(11);
        assertThat(countOf(StudyGroupCategory.UNSPECIFIED)).isEqualTo(1);
    }

    @Test
    void keepsCollegeInvitesProvidedByTheCurators() {
        var links = UkdStudyGroupData.entries().stream()
                .filter(group -> group.category() == StudyGroupCategory.COLLEGE)
                .map(StudyGroup::inviteLink)
                .toList();

        assertThat(links).contains(
                "https://t.me/+XYVIorkFAU05YzQy",
                "https://t.me/+QQobfuw47Kw1Zjky",
                "https://t.me/+AP2SgXSx-x83Mjcy",
                "https://t.me/+AnMT_QFMFY4MjQy");
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
    void containsEveryInviteProvidedByTheCurators() {
        var links = UkdStudyGroupData.entries().stream().map(StudyGroup::inviteLink).toList();

        assertThat(links).contains(
                "https://t.me/+GZsXf7J8r8VmM2My",
                "https://t.me/+o6vCBIYjcuk3ODYy",
                "https://t.me/+TYur5y1rcBIkOTdi",
                "https://t.me/+aIrV8xm-yn85MmE6",
                "https://t.me/+V0a4kWNnk-82MjAy");
    }

    /**
     * «Маркетинг» існує тричі: університет, 2 курс коледжу і набір після 9 класу.
     * Злиття їх в один запис відправило б людей у чужий чат.
     */
    @Test
    void keepsThreeDistinctMarketingGroups() {
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
