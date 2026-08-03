package dev.clowiance.mickey.entity;

<<<<<<< HEAD
import dev.clowiance.mickey.entity.enums.ReportStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
=======
import jakarta.persistence.Entity;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
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
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
<<<<<<< HEAD
    private String chatTitle;
    private String chatUsername;

    private Long reporterId;
    private String reporterUsername;

    private Long targetId;
    private String targetUsername;

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime createdAt;

    private Long resolvedById;
    private String resolvedByUsername;
    private LocalDateTime resolvedAt;
=======
    private Long reporterId;
    private String reporterUsername;
    private Long targetId;
    private String targetUsername;
    private String reason;

    @Builder.Default
    private String status = "PENDING";

    private LocalDateTime createdAt;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
}
