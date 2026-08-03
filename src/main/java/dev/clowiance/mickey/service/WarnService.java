package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.WarnEntity;
import dev.clowiance.mickey.repository.WarnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarnService {

    private final WarnRepository warnRepository;

    public List<WarnEntity> getActiveWarns(Long userId) {
        return warnRepository.findByUserIdAndActiveTrue(userId);
    }

    public List<WarnEntity> getActiveWarns(Long chatId, Long userId) {
        return warnRepository.findByChatIdAndUserIdAndActiveTrue(chatId, userId);
    }

    public WarnEntity addWarn(Long chatId, Long userId, String reason, Long adminId) {
        WarnEntity warn = WarnEntity.builder()
                .chatId(chatId)
                .userId(userId)
                .adminId(adminId)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        return warnRepository.save(warn);
    }

    public void removeWarnById(Long warnId) {
        warnRepository.findById(warnId).ifPresent(warn -> {
            warn.setActive(false);
            warnRepository.save(warn);
        });
    }

    @Scheduled(fixedRateString = "${bot.warn-cleanup-ms:3600000}",
            initialDelayString = "${bot.scheduler-initial-delay-ms:30000}")
    public void checkExpiredWarns() {
        LocalDateTime threshold = LocalDateTime.now().minusWeeks(2);
        List<WarnEntity> activeWarns = warnRepository.findByActiveTrue();
        for (WarnEntity warn : activeWarns) {
            if (warn.getCreatedAt().isBefore(threshold)) {
                warn.setActive(false);
                warnRepository.save(warn);
            }
        }
    }
}
