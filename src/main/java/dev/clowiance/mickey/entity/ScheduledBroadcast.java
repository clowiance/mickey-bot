package dev.clowiance.mickey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_broadcasts", indexes = {
        @Index(name = "idx_broadcast_due", columnList = "sent, status, send_at, next_attempt_at"),
        @Index(name = "idx_broadcast_chat", columnList = "chat_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledBroadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;
    private String text;
    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Builder.Default
    private Boolean sent = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BroadcastStatus status = BroadcastStatus.PENDING;

    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;
    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(length = 1000)
    private String lastError;
}
