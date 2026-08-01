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
@Table(name = "scheduled_broadcasts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledBroadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String text;
    private LocalDateTime sendAt;

    @Builder.Default
    private Boolean sent = false;
}
