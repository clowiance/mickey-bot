package dev.clowiance.mickey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "poll_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollRecord {

    @Id
    private String pollId;

    private Long chatId;
    private String question;
    private LocalDateTime createdAt;
    private Boolean closed;
}
