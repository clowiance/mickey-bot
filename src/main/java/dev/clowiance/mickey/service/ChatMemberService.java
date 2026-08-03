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
<<<<<<< HEAD
        ChatMember member = repository.findById(userId).orElseGet(() -> {
            ChatMember created = ChatMember.builder().userId(userId).joinedAt(LocalDateTime.now()).build();
            return repository.save(created);
        });
=======
        ChatMember member = repository.findById(userId).orElseThrow();
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        member.setWarnCount(member.getWarnCount() + 1);
        repository.save(member);
        return member.getWarnCount();
    }

<<<<<<< HEAD
    public int removeWarn(Long userId) {
        ChatMember member = repository.findById(userId).orElse(null);
        if (member == null) {
            return 0;
        }
        int newCount = Math.max(0, member.getWarnCount() - 1);
        member.setWarnCount(newCount);
        repository.save(member);
        return newCount;
    }

=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    public void resetWarns(Long userId) {
        repository.findById(userId).ifPresent(member -> {
            member.setWarnCount(0);
            repository.save(member);
        });
    }

    public void setMuted(Long userId, boolean muted, LocalDateTime until) {
<<<<<<< HEAD
        repository.findById(userId).ifPresentOrElse(member -> {
            member.setMuted(muted);
            member.setMutedUntil(until);
            repository.save(member);
        }, () -> {
            ChatMember member = ChatMember.builder()
                    .userId(userId)
                    .muted(muted)
                    .mutedUntil(until)
                    .joinedAt(LocalDateTime.now())
                    .build();
            repository.save(member);
=======
        repository.findById(userId).ifPresent(member -> {
            member.setMuted(muted);
            member.setMutedUntil(until);
            repository.save(member);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        });
    }

    public void setBanned(Long userId, boolean banned) {
<<<<<<< HEAD
        repository.findById(userId).ifPresentOrElse(member -> {
            member.setBanned(banned);
            repository.save(member);
        }, () -> {
            ChatMember member = ChatMember.builder()
                    .userId(userId)
                    .banned(banned)
                    .joinedAt(LocalDateTime.now())
                    .build();
            repository.save(member);
=======
        repository.findById(userId).ifPresent(member -> {
            member.setBanned(banned);
            repository.save(member);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        });
    }

    public ChatMember findById(Long userId) {
        return repository.findById(userId).orElse(null);
    }
<<<<<<< HEAD

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
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
}
