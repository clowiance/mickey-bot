package dev.clowiance.mickey.administration.infrastructure.persistence;

import dev.clowiance.mickey.administration.application.ManagedChatSettingsPort;
import dev.clowiance.mickey.administration.domain.ManagedChatSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaManagedChatSettingsAdapter implements ManagedChatSettingsPort {

    private final SpringDataManagedChatSettingsRepository repository;

    @Override
    public Optional<ManagedChatSettings> findByChatId(Long chatId) {
        return repository.findById(chatId).map(this::toDomain);
    }

    @Override
    public List<ManagedChatSettings> findAll() {
        return repository.findAllByOrderByTitleAscChatIdAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ManagedChatSettings save(ManagedChatSettings settings) {
        return toDomain(repository.save(toEntity(settings)));
    }

    private ManagedChatSettings toDomain(ManagedChatSettingsJpaEntity entity) {
        return new ManagedChatSettings(entity.getChatId(), entity.getTitle(), entity.getUsername(),
                entity.getAudience(), entity.getSpecialtyId(), Boolean.TRUE.equals(entity.getWelcomeEnabled()),
                Boolean.TRUE.equals(entity.getModerationEnabled()), Boolean.TRUE.equals(entity.getBroadcastsEnabled()),
                Boolean.TRUE.equals(entity.getActive()));
    }

    private ManagedChatSettingsJpaEntity toEntity(ManagedChatSettings settings) {
        return ManagedChatSettingsJpaEntity.builder()
                .chatId(settings.chatId())
                .title(settings.title())
                .username(settings.username())
                .audience(settings.audience())
                .specialtyId(settings.specialtyId())
                .welcomeEnabled(settings.welcomeEnabled())
                .moderationEnabled(settings.moderationEnabled())
                .broadcastsEnabled(settings.broadcastsEnabled())
                .active(settings.active())
                .build();
    }
}
