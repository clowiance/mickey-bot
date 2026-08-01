package dev.clowiance.mickey.repository.specification;

import dev.clowiance.mickey.entity.ActivityAuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ActivityAuditLogSpecification {

    public static Specification<ActivityAuditLog> byChatId(Long chatId) {
        return (root, query, cb) -> chatId == null ? null : cb.equal(root.get("chatId"), chatId);
    }

    public static Specification<ActivityAuditLog> byUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<ActivityAuditLog> byCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank() || category.equals("ALL") ? null : cb.equal(root.get("actionCategory"), category);
    }

    public static Specification<ActivityAuditLog> byActionType(String actionType) {
        return (root, query, cb) -> actionType == null || actionType.isBlank() ? null : cb.equal(root.get("actionType"), actionType);
    }

    public static Specification<ActivityAuditLog> createdAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> dateTime == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), dateTime);
    }
}
