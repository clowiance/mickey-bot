package dev.clowiance.mickey.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramHtmlTest {
    @Test
    void escapesEveryTelegramHtmlControlCharacter() {
        assertThat(TelegramHtml.escape("Tom & <b>\"Jerry\"</b>"))
                .isEqualTo("Tom &amp; &lt;b&gt;&quot;Jerry&quot;&lt;/b&gt;");
    }
}
