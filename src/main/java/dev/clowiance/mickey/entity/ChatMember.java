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
@Table(name = "chat_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMember {

    @Id
    private Long userId;

    private String username;
    private String firstName;

    private LocalDateTime joinedAt;
    private LocalDateTime lastMessageAt;
}
