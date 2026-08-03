package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatMember;
import dev.clowiance.mickey.repository.ChatMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMemberService {

    private final ChatMemberRepository repository;

    public ChatMember getOrCreate(User user) {
        return repository.findById(user.getId()).orElseGet(() -> {
            ChatMember member = ChatMember.builder()
                    .userId(user.getId())
                    .username(user.getUserName())
                    .firstName(user.getFirstName())
                    .joinedAt(LocalDateTime.now())
                    .build();
            return repository.save(member);
        });
    }

    public ChatMember touch(User user) {
        ChatMember member = getOrCreate(user);
        member.setLastMessageAt(LocalDateTime.now());
        member.setUsername(user.getUserName());
        member.setFirstName(user.getFirstName());
        return repository.save(member);
    }

    public ChatMember findById(Long userId) {
        return repository.findById(userId).orElse(null);
    }

    public ChatMember findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String normalized = username.startsWith("@") ? username.substring(1) : username;
        return repository.findByUsernameIgnoreCase(normalized).orElse(null);
    }

    public long countMembers() {
        return repository.count();
    }

    public long countTotalMembers() {
        return repository.count();
    }
}
