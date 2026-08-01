package dev.clowiance.mickey.administration.application;

import dev.clowiance.mickey.administration.domain.ManagedChatSettings;

import java.util.List;
import java.util.Optional;

public interface ManagedChatSettingsPort {
    Optional<ManagedChatSettings> findByChatId(Long chatId);

    List<ManagedChatSettings> findAll();

    ManagedChatSettings save(ManagedChatSettings settings);
}
