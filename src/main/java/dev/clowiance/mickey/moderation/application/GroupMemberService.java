package dev.clowiance.mickey.moderation.application;

import dev.clowiance.mickey.moderation.domain.GroupMemberState;
import dev.clowiance.mickey.moderation.domain.GroupMemberStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupMemberStatePort statePort;

    @Transactional
    public GroupMemberState register(Long chatId, Long userId, String username, String firstName) {
        var existing = statePort.find(chatId, userId);
        if (existing.isEmpty()) {
            return statePort.save(GroupMemberState.initial(chatId, userId, username, firstName));
        }
        GroupMemberState current = existing.get();
        GroupMemberState updated = current.withIdentity(username, firstName);
        return updated.equals(current) ? current : statePort.save(updated);
    }

    @Transactional
    public GroupMemberState touch(Long chatId, Long userId, String username, String firstName) {
        GroupMemberState state = statePort.find(chatId, userId)
                .orElseGet(() -> GroupMemberState.initial(chatId, userId, username, firstName));
        return statePort.save(state.touched(username, firstName));
    }

    @Transactional
    public int addWarn(Long chatId, Long userId, String username, String firstName) {
        GroupMemberState state = statePort.find(chatId, userId)
                .orElseGet(() -> GroupMemberState.initial(chatId, userId, username, firstName));
        GroupMemberState updated = state.withWarnCount(state.warnCount() + 1);
        statePort.save(updated);
        return updated.warnCount();
    }

    @Transactional
    public void setMuted(Long chatId, Long userId, boolean muted, LocalDateTime until) {
        GroupMemberState state = statePort.find(chatId, userId)
                .orElseGet(() -> GroupMemberState.initial(chatId, userId, null, null));
        statePort.save(state.withMuted(muted, until));
    }

    @Transactional
    public void setBanned(Long chatId, Long userId, boolean banned) {
        GroupMemberState state = statePort.find(chatId, userId)
                .orElseGet(() -> GroupMemberState.initial(chatId, userId, null, null));
        statePort.save(state.withBanned(banned));
    }

    @Transactional(readOnly = true)
    public GroupMemberState find(Long chatId, Long userId) {
        return statePort.find(chatId, userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public GroupMemberStatistics statistics(Long chatId) {
        var members = statePort.findByChatId(chatId);
        return new GroupMemberStatistics(
                members.size(),
                members.stream().mapToLong(GroupMemberState::warnCount).sum(),
                members.stream().filter(GroupMemberState::muted).count(),
                members.stream().filter(GroupMemberState::banned).count());
    }
}
