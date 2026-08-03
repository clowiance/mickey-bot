package dev.clowiance.mickey.moderation.domain;

public record GroupMemberStatistics(
        long memberCount,
        long totalWarnings,
        long mutedCount,
        long bannedCount
) {
    public GroupMemberStatistics plus(GroupMemberStatistics other) {
        return new GroupMemberStatistics(
                memberCount + other.memberCount,
                totalWarnings + other.totalWarnings,
                mutedCount + other.mutedCount,
                bannedCount + other.bannedCount);
    }
}
