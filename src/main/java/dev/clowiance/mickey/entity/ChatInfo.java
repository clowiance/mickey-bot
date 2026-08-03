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
@Table(name = "chat_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatInfo {

    @Id
    private Long chatId;

    private Boolean adminOk;
<<<<<<< HEAD
=======
    private Long botJoinedAt;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

    private LocalDateTime lastWarningAt;
}
