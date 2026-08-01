package dev.clowiance.mickey.moderation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberStateJpaEntity {

    @EmbeddedId
    private GroupMemberStateId id;

    @Column(length = 100)
    private String username;

    @Column(length = 150)
    private String firstName;

    @Column(nullable = false)
    private Integer warnCount;

    @Column(nullable = false)
    private Boolean muted;

    @Column(nullable = false)
    private Boolean banned;

    private LocalDateTime mutedUntil;
    private LocalDateTime joinedAt;
    private LocalDateTime lastMessageAt;
}
