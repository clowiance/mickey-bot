package dev.clowiance.mickey.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandAuthorizationTest {

    @Test
    void publicCommandsAreAvailableToEveryone() {
        assertThat(new CommandAuthorization(false, false).allows(CommandAccess.PUBLIC)).isTrue();
    }

    @Test
    void chatAdminCannotUseDeveloperCommands() {
        CommandAuthorization authorization = new CommandAuthorization(false, true);

        assertThat(authorization.allows(CommandAccess.CHAT_ADMIN)).isTrue();
        assertThat(authorization.allows(CommandAccess.DEVELOPER)).isFalse();
    }

    @Test
    void developerCanUseEveryCommand() {
        CommandAuthorization authorization = new CommandAuthorization(true, false);

        assertThat(authorization.allows(CommandAccess.PUBLIC)).isTrue();
        assertThat(authorization.allows(CommandAccess.CHAT_ADMIN)).isTrue();
        assertThat(authorization.allows(CommandAccess.DEVELOPER)).isTrue();
    }
}
