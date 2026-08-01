package dev.clowiance.mickey.administration.infrastructure.persistence;

import dev.clowiance.mickey.administration.domain.ChatAudience;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "managed_chat_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedChatSettingsJpaEntity {

    @Id
    private Long chatId;

    @Column(length = 250)
    private String title;

    @Column(length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChatAudience audience;

    private Long specialtyId;

    @Column(nullable = false)
    private Boolean welcomeEnabled;

    @Column(nullable = false)
    private Boolean moderationEnabled;

    @Column(nullable = false)
    private Boolean broadcastsEnabled;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}
