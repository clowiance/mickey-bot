package dev.clowiance.mickey.command;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ловить розходження між зареєстрованими командами й тим, що бот про них розповідає.
 * Саме таке розходження й накопичилося раніше: /help рекламував /adminpanel і /devpanel,
 * яких у коді немає, і мовчав про /charts, /groupadmin, /economy та /chat.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "bot.polling-enabled=false",
        "bot.token=test-token",
        "bot.username=test_bot"
})
class CommandCatalogTest {

    @Autowired
    private List<ICommand> commands;

    @Test
    void describesEveryRegisteredCommand() {
        Set<String> registered = commands.stream().map(ICommand::getCommand).collect(Collectors.toSet());

        assertThat(CommandCatalog.entries().keySet())
                .as("кожна команда бота повинна мати опис у каталозі")
                .containsExactlyInAnyOrderElementsOf(registered);
    }

    @Test
    void doesNotDescribeCommandsThatNoLongerExist() {
        Set<String> registered = commands.stream().map(ICommand::getCommand).collect(Collectors.toSet());

        assertThat(registered)
                .as("каталог не повинен обіцяти команди, яких немає")
                .containsAll(CommandCatalog.entries().keySet());
    }

    @Test
    void keepsDescriptionsWithinTelegramLimit() {
        assertThat(CommandCatalog.entries().values())
                .allSatisfy(entry -> assertThat(entry.description()).isNotBlank().hasSizeLessThanOrEqualTo(256));
    }

    @Test
    void usesNamesTelegramAccepts() {
        assertThat(CommandCatalog.entries().keySet())
                .allSatisfy(command -> assertThat(command.substring(1)).matches("[a-z0-9_]{1,32}"));
    }
}
