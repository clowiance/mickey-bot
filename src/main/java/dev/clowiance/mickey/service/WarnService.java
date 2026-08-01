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

    public WarnEntity addWarn(Long userId, String reason, Long adminId) {
        WarnEntity warn = WarnEntity.builder()
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

    @Scheduled(fixedRate = 60 * 60 * 1000L)
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
