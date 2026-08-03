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
<<<<<<< HEAD
=======
    private Long botJoinedAt;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

    private LocalDateTime lastWarningAt;
}
