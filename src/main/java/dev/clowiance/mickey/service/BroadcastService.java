package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ScheduledBroadcast;
import dev.clowiance.mickey.entity.BroadcastStatus;
import dev.clowiance.mickey.repository.ScheduledBroadcastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BroadcastService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_TEXT_LENGTH = 3900;
    private static final List<BroadcastStatus> CLAIMABLE = List.of(BroadcastStatus.PENDING, BroadcastStatus.FAILED);

    private final ScheduledBroadcastRepository repository;

    public long count(BroadcastStatus status) {
        return repository.countByStatus(status);
    }

    public List<ScheduledBroadcast> recent() {
        return repository.findTop100ByOrderBySendAtDesc();
    }

    @Transactional
    public void cancel(Long id) {
        ScheduledBroadcast broadcast = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Розсилку не знайдено: " + id));
        cancel(broadcast, "Скасовано адміністратором");
    }

    @Transactional
    public void retry(Long id) {
        ScheduledBroadcast broadcast = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Розсилку не знайдено: " + id));
        if (broadcast.getStatus() != BroadcastStatus.FAILED
                && broadcast.getStatus() != BroadcastStatus.DEAD_LETTER) {
            throw new IllegalStateException("Повторити можна лише невдалу розсилку");
        }
        broadcast.setSent(false);
        broadcast.setStatus(BroadcastStatus.FAILED);
        broadcast.setNextAttemptAt(LocalDateTime.now());
        broadcast.setProcessingStartedAt(null);
        broadcast.setLastError(null);
        repository.save(broadcast);
    }

    public ScheduledBroadcast schedule(Long chatId, String text, LocalDateTime sendAt) {
        if (chatId == null || sendAt == null) {
            throw new IllegalArgumentException("Чат і час розсилки обов'язкові");
        }
        if (text == null || text.isBlank() || text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Текст розсилки має містити від 1 до " + MAX_TEXT_LENGTH + " символів");
        }
        ScheduledBroadcast broadcast = ScheduledBroadcast.builder()
                .chatId(chatId)
                .text(text)
                .sendAt(sendAt)
                .sent(false)
                .status(BroadcastStatus.PENDING)
                .attemptCount(0)
                .build();
        return repository.save(broadcast);
    }

    @Transactional
    public List<ScheduledBroadcast> claimDue(int limit) {
        LocalDateTime now = LocalDateTime.now();
        repository.recoverStale(BroadcastStatus.PROCESSING, BroadcastStatus.FAILED,
                now.minusMinutes(10), now, "Попередня спроба зависла і була відновлена");
        List<Long> ids = repository.findDueIds(now, CLAIMABLE, PageRequest.of(0, Math.max(1, Math.min(limit, 100))));
        List<ScheduledBroadcast> claimed = new ArrayList<>();
        for (Long id : ids) {
            if (repository.claim(id, now, BroadcastStatus.PROCESSING, CLAIMABLE) == 1) {
                repository.findById(id).ifPresent(claimed::add);
            }
        }
        return claimed;
    }

    @Transactional
    public void markSent(ScheduledBroadcast broadcast) {
        broadcast.setSent(true);
        broadcast.setStatus(BroadcastStatus.SENT);
        broadcast.setProcessingStartedAt(null);
        broadcast.setNextAttemptAt(null);
        broadcast.setLastError(null);
        repository.save(broadcast);
    }

    @Transactional
    public void markFailed(ScheduledBroadcast broadcast, Exception exception) {
        int attempts = (broadcast.getAttemptCount() == null ? 0 : broadcast.getAttemptCount()) + 1;
        broadcast.setAttemptCount(attempts);
        broadcast.setProcessingStartedAt(null);
        broadcast.setLastAttemptAt(LocalDateTime.now());
        broadcast.setLastError(truncate(exception.getMessage(), 1000));
        if (attempts >= MAX_ATTEMPTS) {
            broadcast.setStatus(BroadcastStatus.DEAD_LETTER);
            broadcast.setNextAttemptAt(null);
        } else {
            long delayMinutes = Math.min(60L, 1L << Math.min(attempts - 1, 6));
            broadcast.setStatus(BroadcastStatus.FAILED);
            broadcast.setNextAttemptAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }
        repository.save(broadcast);
    }

    @Transactional
    public void cancel(ScheduledBroadcast broadcast, String reason) {
        broadcast.setStatus(BroadcastStatus.CANCELLED);
        broadcast.setSent(true);
        broadcast.setProcessingStartedAt(null);
        broadcast.setLastError(truncate(reason, 1000));
        repository.save(broadcast);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "Невідома помилка";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
