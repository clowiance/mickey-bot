package dev.clowiance.mickey.analytics.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChartDashboardTest {

    @Test
    void recognisesEverySlugRegardlessOfCase() {
        assertThat(ChartDashboard.fromSlug("group")).contains(ChartDashboard.GROUP);
        assertThat(ChartDashboard.fromSlug("ACTIVITY")).contains(ChartDashboard.ACTIVITY);
        assertThat(ChartDashboard.fromSlug("  Earned ")).contains(ChartDashboard.EARNED);
    }

    /**
     * Раніше {@code /charts global} мовчки перетворювався на зведення по групі —
     * тепер невідоме й недоступне значення розрізняються від валідного.
     */
    @Test
    void recognisesGlobalInsteadOfSilentlyFallingBack() {
        assertThat(ChartDashboard.fromSlug("global")).contains(ChartDashboard.GLOBAL);
    }

    @Test
    void rejectsUnknownValueInsteadOfGuessing() {
        assertThat(ChartDashboard.fromSlug("статистика")).isEmpty();
        assertThat(ChartDashboard.fromSlug("")).isEmpty();
        assertThat(ChartDashboard.fromSlug(null)).isEmpty();
    }

    @Test
    void marksOnlyGlobalAsDeveloperOnly() {
        assertThat(ChartDashboard.GLOBAL.developerOnly()).isTrue();
        assertThat(ChartDashboard.GROUP.developerOnly()).isFalse();
        assertThat(ChartDashboard.ACTIVITY.developerOnly()).isFalse();
        assertThat(ChartDashboard.RATING.developerOnly()).isFalse();
        assertThat(ChartDashboard.EARNED.developerOnly()).isFalse();
    }

    /**
     * Діаграми, що не залежать від чату, мають кешуватися однією копією на всіх;
     * інакше кожен чат перерендерював би той самий PNG.
     */
    @Test
    void marksChatIndependentDashboardsAsGlobalScope() {
        assertThat(ChartDashboard.GROUP.globalScope()).isFalse();
        assertThat(ChartDashboard.ACTIVITY.globalScope()).isFalse();
        assertThat(ChartDashboard.RATING.globalScope()).isTrue();
        assertThat(ChartDashboard.EARNED.globalScope()).isTrue();
        assertThat(ChartDashboard.GLOBAL.globalScope()).isTrue();
    }

    @Test
    void hidesDeveloperDashboardFromAdminUsage() {
        String adminUsage = ChartDashboard.usage(false);

        assertThat(adminUsage).contains("/charts group", "/charts activity", "/charts earned");
        assertThat(adminUsage).doesNotContain("/charts global");
    }

    @Test
    void listsEveryDashboardForDeveloper() {
        String devUsage = ChartDashboard.usage(true);

        for (ChartDashboard dashboard : ChartDashboard.values()) {
            assertThat(devUsage)
                    .as("підказка має згадувати %s", dashboard.slug())
                    .contains("/charts " + dashboard.slug());
        }
    }
}
