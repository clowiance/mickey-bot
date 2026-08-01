package dev.clowiance.mickey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private Long userId;

    private String username;

    private String actionCategory;

    private String actionType;

    private String details;

    private LocalDateTime createdAt;
}
