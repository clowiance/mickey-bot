package dev.clowiance.mickey.administration.domain;

public record ManagedChatSettings(
        Long chatId,
        String title,
        String username,
        ChatAudience audience,
        Long specialtyId,
        boolean welcomeEnabled,
        boolean moderationEnabled,
        boolean broadcastsEnabled,
        boolean active
) {
    public ManagedChatSettings {
        if (chatId == null) {
            throw new IllegalArgumentException("chatId is required");
        }
        if (audience == null) {
            throw new IllegalArgumentException("audience is required");
        }
    }

    public static ManagedChatSettings defaults(Long chatId, String title, String username) {
        return new ManagedChatSettings(chatId, title, username, ChatAudience.MIXED, null,
                true, true, true, true);
    }

    public ManagedChatSettings withMetadata(String newTitle, String newUsername) {
        return new ManagedChatSettings(chatId,
                hasText(newTitle) ? newTitle : title,
                hasText(newUsername) ? newUsername : username,
                audience, specialtyId, welcomeEnabled, moderationEnabled, broadcastsEnabled, active);
    }

    public ManagedChatSettings toggle(ChatFeature feature) {
        return new ManagedChatSettings(chatId, title, username, audience, specialtyId,
                feature == ChatFeature.WELCOME ? !welcomeEnabled : welcomeEnabled,
                feature == ChatFeature.MODERATION ? !moderationEnabled : moderationEnabled,
                feature == ChatFeature.BROADCASTS ? !broadcastsEnabled : broadcastsEnabled,
                active);
    }

    public ManagedChatSettings withAudience(ChatAudience newAudience) {
        return new ManagedChatSettings(chatId, title, username, newAudience, specialtyId,
                welcomeEnabled, moderationEnabled, broadcastsEnabled, active);
    }

    public ManagedChatSettings withSpecialty(Long newSpecialtyId) {
        return new ManagedChatSettings(chatId, title, username, audience, newSpecialtyId,
                welcomeEnabled, moderationEnabled, broadcastsEnabled, active);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
