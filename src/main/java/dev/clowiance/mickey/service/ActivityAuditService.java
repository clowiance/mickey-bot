package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ActivityAuditLog;
import dev.clowiance.mickey.repository.ActivityAuditLogRepository;
import dev.clowiance.mickey.repository.specification.ActivityAuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityAuditService {

    private final ActivityAuditLogRepository repository;

    public void logActivity(Long chatId, Long userId, String username, String category, String actionType, String details) {
        try {
            ActivityAuditLog logEntry = ActivityAuditLog.builder()
                    .chatId(chatId)
                    .userId(userId)
                    .username(username)
                    .actionCategory(category)
                    .actionType(actionType)
                    .details(details)
                    .createdAt(LocalDateTime.now())
                    .build();
            repository.save(logEntry);
        } catch (Exception ignored) {
        }
    }

    public List<ActivityAuditLog> queryLogs(Long chatId, Long userId, String category, String actionType, LocalDateTime after, int limit) {
        Specification<ActivityAuditLog> spec = Specification.where(ActivityAuditLogSpecification.byChatId(chatId))
                .and(ActivityAuditLogSpecification.byUserId(userId))
                .and(ActivityAuditLogSpecification.byCategory(category))
                .and(ActivityAuditLogSpecification.byActionType(actionType))
                .and(ActivityAuditLogSpecification.createdAfter(after));

        return repository.findAll(
                spec,
                PageRequest.of(0, Math.min(Math.max(limit, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    public LogsPage queryLogsPaged(Long chatId, String category, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        Specification<ActivityAuditLog> spec = Specification.where(ActivityAuditLogSpecification.byChatId(chatId))
                .and(ActivityAuditLogSpecification.byCategory(category));

        List<ActivityAuditLog> content = repository.findAll(
                spec,
                PageRequest.of(safePage, safeSize, sort)
        ).getContent();

        boolean hasNext = false;
        if (content.size() == safeSize) {
            hasNext = !repository.findAll(spec, PageRequest.of(safePage + 1, 1, sort)).getContent().isEmpty();
        }

        return new LogsPage(content, safePage, hasNext, safePage > 0);
    }

    public record LogsPage(List<ActivityAuditLog> content, int page, boolean hasNext, boolean hasPrev) {
    }
}
