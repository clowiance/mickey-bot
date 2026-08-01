package dev.clowiance.mickey.command;

public record CommandAuthorization(boolean developer, boolean chatAdmin) {

    public boolean allows(CommandAccess access) {
        return switch (access) {
            case PUBLIC -> true;
            case CHAT_ADMIN -> developer || chatAdmin;
            case DEVELOPER -> developer;
        };
    }
}
