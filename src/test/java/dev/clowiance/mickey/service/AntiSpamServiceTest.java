package dev.clowiance.mickey.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AntiSpamServiceTest {

    @Test
    void keepsDuplicateHistoryIndependentBetweenChats() {
        AntiSpamService service = new AntiSpamService();
        String text = "same repeated message";

        assertThat(service.isDuplicateOrSimilarSpam(100L, 7L, text)).isFalse();
        assertThat(service.isDuplicateOrSimilarSpam(100L, 7L, text)).isFalse();
        assertThat(service.isDuplicateOrSimilarSpam(200L, 7L, text)).isFalse();
        assertThat(service.isDuplicateOrSimilarSpam(100L, 7L, text)).isTrue();
        assertThat(service.isDuplicateOrSimilarSpam(200L, 7L, text)).isFalse();
    }

    @Test
    void keepsFloodCountersIndependentBetweenChats() {
        AntiSpamService service = new AntiSpamService();

        for (int index = 0; index < 3; index++) {
            assertThat(service.isFastSending(100L, 7L)).isFalse();
        }
        assertThat(service.isFastSending(200L, 7L)).isFalse();
        assertThat(service.isFastSending(100L, 7L)).isTrue();
    }
}
