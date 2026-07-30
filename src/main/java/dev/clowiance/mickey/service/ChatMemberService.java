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

    public int addWarn(Long userId) {
        ChatMember member = repository.findById(userId).orElseThrow();
        member.setWarnCount(member.getWarnCount() + 1);
        repository.save(member);
        return member.getWarnCount();
    }

    public void resetWarns(Long userId) {
        repository.findById(userId).ifPresent(member -> {
            member.setWarnCount(0);
            repository.save(member);
        });
    }

    public void setMuted(Long userId, boolean muted, LocalDateTime until) {
        repository.findById(userId).ifPresent(member -> {
            member.setMuted(muted);
            member.setMutedUntil(until);
            repository.save(member);
        });
    }

    public void setBanned(Long userId, boolean banned) {
        repository.findById(userId).ifPresent(member -> {
            member.setBanned(banned);
            repository.save(member);
        });
    }

    public ChatMember findById(Long userId) {
        return repository.findById(userId).orElse(null);
    }
}
