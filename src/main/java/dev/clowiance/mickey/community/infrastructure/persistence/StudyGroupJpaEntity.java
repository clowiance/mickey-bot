package dev.clowiance.mickey.community.infrastructure.persistence;

import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "managed_study_groups", indexes = {
        @Index(name = "idx_study_group_active_category", columnList = "active, category"),
        @Index(name = "idx_study_group_specialty", columnList = "specialty")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class StudyGroupJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudyGroupCategory category;

    @Column(nullable = false, length = 250)
    private String specialty;

    @Column(length = 100)
    private String course;

    @Column(name = "invite_link", nullable = false, unique = true, length = 200)
    private String inviteLink;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
