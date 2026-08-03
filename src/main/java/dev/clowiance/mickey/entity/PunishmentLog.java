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
@Table(name = "punishment_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunishmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @jakarta.persistence.Column(columnDefinition = "integer")
    private Long id;

    private Long chatId;
    private Long userId;
    private String username;
    private String type;
    private String reason;
    private Long adminId;
    private LocalDateTime createdAt;
}
