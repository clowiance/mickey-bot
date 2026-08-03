package dev.clowiance.mickey.moderation.domain;

import java.time.LocalDateTime;

public record GroupMemberState(
        Long chatId,
        Long userId,
        String username,
        String firstName,
        int warnCount,
        boolean muted,
        boolean banned,
        LocalDateTime mutedUntil,
        LocalDateTime joinedAt,
        LocalDateTime lastMessageAt
) {
    public GroupMemberState {
        if (chatId == null || userId == null) {
            throw new IllegalArgumentException("chatId and userId are required");
        }
        if (warnCount < 0) {
            throw new IllegalArgumentException("warnCount cannot be negative");
        }
    }

    public static GroupMemberState initial(Long chatId, Long userId, String username, String firstName) {
        return new GroupMemberState(chatId, userId, username, firstName, 0,
                false, false, null, LocalDateTime.now(), null);
    }

    public GroupMemberState withIdentity(String newUsername, String newFirstName) {
        return new GroupMemberState(chatId, userId, newUsername, newFirstName, warnCount,
                muted, banned, mutedUntil, joinedAt, lastMessageAt);
    }

    public GroupMemberState touched(String newUsername, String newFirstName) {
        return new GroupMemberState(chatId, userId, newUsername, newFirstName, warnCount,
                muted, banned, mutedUntil, joinedAt, LocalDateTime.now());
    }

    public GroupMemberState withWarnCount(int newWarnCount) {
        return new GroupMemberState(chatId, userId, username, firstName, newWarnCount,
                muted, banned, mutedUntil, joinedAt, lastMessageAt);
    }

    public GroupMemberState withMuted(boolean newMuted, LocalDateTime until) {
        return new GroupMemberState(chatId, userId, username, firstName, warnCount,
                newMuted, banned, until, joinedAt, lastMessageAt);
    }

    public GroupMemberState withBanned(boolean newBanned) {
        return new GroupMemberState(chatId, userId, username, firstName, warnCount,
                muted, newBanned, mutedUntil, joinedAt, lastMessageAt);
    }
}
