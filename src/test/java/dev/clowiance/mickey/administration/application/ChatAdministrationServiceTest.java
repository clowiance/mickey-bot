package dev.clowiance.mickey.administration.application;

import dev.clowiance.mickey.administration.domain.ChatAudience;
import dev.clowiance.mickey.administration.domain.ChatFeature;
import dev.clowiance.mickey.administration.domain.ManagedChatSettings;
import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAdministrationServiceTest {

    @Mock
    private ManagedChatSettingsPort settingsPort;

    @Mock
    private UniversityCatalogService catalogService;

    private ChatAdministrationService service;

    @BeforeEach
    void setUp() {
        service = new ChatAdministrationService(settingsPort, catalogService);
        lenient().when(settingsPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsIndependentDefaultsForNewChat() {
        when(settingsPort.findByChatId(100L)).thenReturn(Optional.empty());

        ManagedChatSettings settings = service.ensureChat(100L, "F2 Software", "f2_group");

        assertThat(settings.chatId()).isEqualTo(100L);
        assertThat(settings.audience()).isEqualTo(ChatAudience.MIXED);
        assertThat(settings.welcomeEnabled()).isTrue();
        assertThat(settings.moderationEnabled()).isTrue();
        assertThat(settings.broadcastsEnabled()).isTrue();
    }

    @Test
    void togglesOnlyRequestedFeature() {
        ManagedChatSettings current = ManagedChatSettings.defaults(100L, "Group", null);
        when(settingsPort.findByChatId(100L)).thenReturn(Optional.of(current));

        ManagedChatSettings updated = service.toggle(100L, ChatFeature.MODERATION);

        assertThat(updated.moderationEnabled()).isFalse();
        assertThat(updated.welcomeEnabled()).isTrue();
        assertThat(updated.broadcastsEnabled()).isTrue();
    }

    @Test
    void rejectsUnknownSpecialtyAssignment() {
        when(catalogService.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignSpecialty(100L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Specialty not found");
    }
}
