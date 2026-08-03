package dev.clowiance.mickey.administration.application;

import dev.clowiance.mickey.administration.domain.ChatFeature;
import dev.clowiance.mickey.administration.domain.ManagedChatSettings;
import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatAdministrationService {

    private final ManagedChatSettingsPort settingsPort;
    private final UniversityCatalogService catalogService;

    @Transactional
    public ManagedChatSettings ensureChat(Long chatId, String title, String username) {
        var existing = settingsPort.findByChatId(chatId);
        if (existing.isEmpty()) {
            return settingsPort.save(ManagedChatSettings.defaults(chatId, title, username));
        }
        ManagedChatSettings current = existing.get();
        ManagedChatSettings updated = current.withMetadata(title, username);
        return updated.equals(current) ? current : settingsPort.save(updated);
    }

    @Transactional
    public ManagedChatSettings getOrCreate(Long chatId) {
        return settingsPort.findByChatId(chatId)
                .orElseGet(() -> settingsPort.save(ManagedChatSettings.defaults(chatId, null, null)));
    }

    @Transactional(readOnly = true)
    public List<ManagedChatSettings> findAll() {
        return settingsPort.findAll();
    }

    @Transactional
    public ManagedChatSettings toggle(Long chatId, ChatFeature feature) {
        ManagedChatSettings settings = getOrCreate(chatId).toggle(feature);
        return settingsPort.save(settings);
    }

    @Transactional
    public ManagedChatSettings cycleAudience(Long chatId) {
        ManagedChatSettings settings = getOrCreate(chatId);
        return settingsPort.save(settings.withAudience(settings.audience().next()));
    }

    @Transactional
    public ManagedChatSettings assignSpecialty(Long chatId, Long specialtyId) {
        if (specialtyId != null && catalogService.findById(specialtyId).isEmpty()) {
            throw new IllegalArgumentException("Specialty not found: " + specialtyId);
        }
        return settingsPort.save(getOrCreate(chatId).withSpecialty(specialtyId));
    }

    @Transactional
    public boolean isEnabled(Long chatId, ChatFeature feature) {
        ManagedChatSettings settings = getOrCreate(chatId);
        return switch (feature) {
            case WELCOME -> settings.welcomeEnabled();
            case MODERATION -> settings.moderationEnabled();
            case BROADCASTS -> settings.broadcastsEnabled();
        };
    }
}
