package dev.clowiance.mickey.scheduler;

import dev.clowiance.mickey.repository.ActivityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRetentionScheduler {

    private final ActivityAuditLogRepository repository;

    @Value("${audit.retention-days:30}")
    private int retentionDays;

    @Transactional
    @Scheduled(cron = "${audit.cleanup-cron:0 20 3 * * *}")
    public void deleteExpiredLogs() {
        int safeRetentionDays = Math.max(1, retentionDays);
        long deleted = repository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(safeRetentionDays));
        if (deleted > 0) {
            log.info("Видалено {} аудит-записів старше {} днів", deleted, safeRetentionDays);
        }
    }
}
