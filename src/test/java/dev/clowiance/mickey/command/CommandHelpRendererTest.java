package dev.clowiance.mickey.command;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandHelpRendererTest {

    private final CommandHelpRenderer renderer = new CommandHelpRenderer(List.of(
            stub("/start", CommandAccess.PUBLIC_INFO, false),
            stub("/poll", CommandAccess.GROUP_MEMBER, true),
            stub("/ban", CommandAccess.GROUP_ADMIN, true),
            stub("/economy", CommandAccess.DEVELOPER, false)));

    @Test
    void hidesAdminAndDeveloperCommandsFromRegularMember() {
        String help = renderer.render(false, false, false);

        assertThat(help).contains("/start", "/poll");
        assertThat(help).doesNotContain("/ban", "/economy");
    }

    @Test
    void showsModerationToChatAdminButNotDeveloperTools() {
        String help = renderer.render(true, false, false);

        assertThat(help).contains("/ban");
        assertThat(help).doesNotContain("/economy");
    }

    @Test
    void showsEverythingToDeveloper() {
        String help = renderer.render(false, true, false);

        assertThat(help).contains("/start", "/poll", "/ban", "/economy");
    }

    @Test
    void inPrivateChatPromisesOnlyWhatBotAcceptsThere() {
        String help = renderer.render(false, false, true);

        assertThat(help).contains("/start");
        // /poll — групова, /ban і /economy у ЛС звичайному учаснику все одно відмовлять.
        assertThat(help).doesNotContain("/poll", "/ban", "/economy");
    }

    @Test
    void groupsCommandsUnderSectionHeadings() {
        String help = renderer.render(true, true, false);

        assertThat(help).contains(CommandCatalog.Section.BASIC.title());
        assertThat(help).contains(CommandCatalog.Section.MODERATION.title());
    }

    private ICommand stub(String name, CommandAccess access, boolean groupOnly) {
        return new ICommand() {
            @Override
            public String getCommand() {
                return name;
            }

            @Override
            public void execute(TelegramLongPollingBot bot, Message message, String args) {
            }

            @Override
            public boolean isAdminOnly() {
                return access == CommandAccess.GROUP_ADMIN;
            }

            @Override
            public boolean isDevOnly() {
                return access == CommandAccess.DEVELOPER;
            }

            @Override
            public boolean isGroupOnly() {
                return groupOnly;
            }

            @Override
            public boolean isInfoCommand() {
                return access == CommandAccess.PUBLIC_INFO;
            }
        };
    }
}
