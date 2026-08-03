package dev.clowiance.mickey.catalog.infrastructure.persistence;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.TuitionPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "ukd_specialties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "integer")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String catalogKey;

    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(length = 300)
    private String educationalProgram;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EducationLevel educationLevel;

    private Long fullTimeTuition;
    private Long partTimeTuition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TuitionPeriod tuitionPeriod;

    @Column(length = 600)
    private String tuitionNote;

    @Column(nullable = false, length = 300)
    private String sourceUrl;

    @Column(nullable = false)
    private Integer sourceYear;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}
