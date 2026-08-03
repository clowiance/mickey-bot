package dev.clowiance.mickey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "economy_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EconomyAccount {

    @Id
    private Long userId;

    @Builder.Default
    private Long balance = 0L;

    @Builder.Default
    private Long totalEarned = 0L;

    @Builder.Default
    private Integer dailyStreak = 0;

    private LocalDateTime lastDailyAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
