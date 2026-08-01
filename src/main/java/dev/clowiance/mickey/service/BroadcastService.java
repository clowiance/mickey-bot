package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ScheduledBroadcast;
import dev.clowiance.mickey.repository.ScheduledBroadcastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final ScheduledBroadcastRepository repository;

    public ScheduledBroadcast schedule(Long chatId, String text, LocalDateTime sendAt) {
        ScheduledBroadcast broadcast = ScheduledBroadcast.builder()
                .chatId(chatId)
                .text(text)
                .sendAt(sendAt)
                .sent(false)
                .build();
        return repository.save(broadcast);
    }

    public List<ScheduledBroadcast> findDue() {
        return repository.findBySentFalseAndSendAtBefore(LocalDateTime.now());
    }

    public void markSent(ScheduledBroadcast broadcast) {
        broadcast.setSent(true);
        repository.save(broadcast);
    }
}
