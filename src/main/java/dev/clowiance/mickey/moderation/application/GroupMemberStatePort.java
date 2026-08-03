package dev.clowiance.mickey.moderation.application;

import dev.clowiance.mickey.moderation.domain.GroupMemberState;

import java.util.List;
import java.util.Optional;

public interface GroupMemberStatePort {
    Optional<GroupMemberState> find(Long chatId, Long userId);

    List<GroupMemberState> findByChatId(Long chatId);

    GroupMemberState save(GroupMemberState state);
}
