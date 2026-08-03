package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.bootstrap.UkdStudyGroupData;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyGroupMessageRendererTest {

    private static final int TELEGRAM_MESSAGE_LIMIT = 4096;

    private final StudyGroupMessageRenderer renderer = new StudyGroupMessageRenderer();

    @Test
    void showsHowManyGroupsEachCategoryHas() {
        String menu = renderer.renderMenu();

        assertThat(menu).contains("Університет (УНІК) — 7")
                .contains("Фаховий коледж — 11")
                .contains("Усього груп: 19");
    }

    @Test
    void listsOnlyGroupsOfRequestedCategory() {
        String university = renderer.renderCategory(StudyGroupCategory.UNIVERSITY);

        assertThat(university).contains("Логістика").contains("Право");
        assertThat(university).doesNotContain("Хореографія").doesNotContain("Юридичне діловодство");
    }

    @Test
    void splitsCollegeGroupsByEntryBase() {
        String college = renderer.renderCategory(StudyGroupCategory.COLLEGE);

        assertThat(college).contains("2 курс")
                .contains("Після 9 класу")
                .contains("Курс не вказано");
        assertThat(college.indexOf("2 курс"))
                .as("курси йдуть по порядку, «не вказано» — наприкінці")
                .isLessThan(college.indexOf("Курс не вказано"));
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
    void separatesUniversityFromCollegeInFullList() {
        String all = renderer.renderAll();

        assertThat(all.indexOf("Університет (УНІК)")).isLessThan(all.indexOf("Фаховий коледж"));
    }

    @Test
    void keepsEveryViewWithinTelegramMessageLimit() {
        assertThat(renderer.renderMenu().length()).isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        assertThat(renderer.renderAll().length()).isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        for (StudyGroupCategory category : StudyGroupCategory.values()) {
            assertThat(renderer.renderCategory(category).length())
                    .as("розділ %s", category)
                    .isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        }
    }
}
