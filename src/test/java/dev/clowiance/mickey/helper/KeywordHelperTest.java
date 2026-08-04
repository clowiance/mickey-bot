package dev.clowiance.mickey.helper;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordHelperTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-08-03T10:00:00Z"));
    private final KeywordHelper helper = new KeywordHelper(clock);

    @Test
    void answersKeywordOnce() {
        assertThat(helper.findResponse(-100L, "скільки коштує навчання?")).isPresent();
    }

    @Test
    void doesNotRepeatSameTopicWhileCooldownLasts() {
        assertThat(helper.findResponse(-100L, "яка ціна навчання?")).isPresent();

        assertThat(helper.findResponse(-100L, "а ціна за заочку?")).isEmpty();
        clock.advance(Duration.ofMinutes(14));
        assertThat(helper.findResponse(-100L, "ще раз про вартість")).isEmpty();
    }

    @Test
    void answersAgainAfterCooldownExpires() {
        assertThat(helper.findResponse(-100L, "яка ціна?")).isPresent();
        clock.advance(Duration.ofMinutes(16));

        assertThat(helper.findResponse(-100L, "яка ціна?")).isPresent();
    }

    @Test
    void cooldownOfOneTopicDoesNotSilenceAnother() {
        assertThat(helper.findResponse(-100L, "яка ціна?")).isPresent();

        assertThat(helper.findResponse(-100L, "а гуртожиток є?")).isPresent();
    }

    @Test
    void cooldownIsPerChat() {
        assertThat(helper.findResponse(-100L, "яка ціна?")).isPresent();

        assertThat(helper.findResponse(-200L, "яка ціна?")).isPresent();
    }

    /**
     * Про строки вступної кампанії бот більше не висловлюється: дати щороку змінюються,
     * і мовчання безпечніше за застарілий дедлайн, надісланий абітурієнту.
     */
    @Test
    void staysSilentAboutAdmissionCampaignDeadlines() {
        assertThat(helper.findResponse(-100L, "які дедлайни цього року?")).isEmpty();
        assertThat(helper.findResponse(-100L, "коли підписувати договір?")).isEmpty();
    }

    @Test
    void explainsDifferenceBetweenDegrees() {
        assertThat(helper.findResponse(-100L, "чим відрізняється бакалавр від магістра?"))
                .contains(TextHelper.FAQ_DEGREES);
    }

    @Test
    void ignoresTextWithoutKeywords() {
        assertThat(helper.findResponse(-100L, "всім привіт")).isEmpty();
        assertThat(helper.findResponse(-100L, null)).isEmpty();
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        private void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
