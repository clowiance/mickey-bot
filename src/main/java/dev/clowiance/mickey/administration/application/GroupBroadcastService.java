package dev.clowiance.mickey.administration.application;

import dev.clowiance.mickey.administration.domain.ManagedChatSettings;
import dev.clowiance.mickey.service.BroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupBroadcastService {

    private final ChatAdministrationService chatAdministrationService;
    private final BroadcastService broadcastService;

    @Transactional
    public int schedule(Long specialtyId, String text, LocalDateTime sendAt) {
        int scheduled = 0;
        for (ManagedChatSettings chat : chatAdministrationService.findAll()) {
            boolean targetMatches = specialtyId == null || specialtyId.equals(chat.specialtyId());
            if (chat.active() && chat.broadcastsEnabled() && targetMatches) {
                broadcastService.schedule(chat.chatId(), text, sendAt);
                scheduled++;
            }
        }
        return scheduled;
    }
}
