package dev.clowiance.mickey.moderation.infrastructure.persistence;

import dev.clowiance.mickey.moderation.application.GroupMemberStatePort;
import dev.clowiance.mickey.moderation.domain.GroupMemberState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaGroupMemberStateAdapter implements GroupMemberStatePort {

    private final SpringDataGroupMemberStateRepository repository;

    @Override
    public Optional<GroupMemberState> find(Long chatId, Long userId) {
        return repository.findById(new GroupMemberStateId(chatId, userId)).map(this::toDomain);
    }

    @Override
    public List<GroupMemberState> findByChatId(Long chatId) {
        return repository.findByIdChatId(chatId).stream().map(this::toDomain).toList();
    }

    @Override
    public GroupMemberState save(GroupMemberState state) {
        return toDomain(repository.save(toEntity(state)));
    }

    private GroupMemberState toDomain(GroupMemberStateJpaEntity entity) {
        return new GroupMemberState(entity.getId().getChatId(), entity.getId().getUserId(),
                entity.getUsername(), entity.getFirstName(), entity.getWarnCount(),
                Boolean.TRUE.equals(entity.getMuted()), Boolean.TRUE.equals(entity.getBanned()),
                entity.getMutedUntil(), entity.getJoinedAt(), entity.getLastMessageAt());
    }

    private GroupMemberStateJpaEntity toEntity(GroupMemberState state) {
        return GroupMemberStateJpaEntity.builder()
                .id(new GroupMemberStateId(state.chatId(), state.userId()))
                .username(state.username())
                .firstName(state.firstName())
                .warnCount(state.warnCount())
                .muted(state.muted())
                .banned(state.banned())
                .mutedUntil(state.mutedUntil())
                .joinedAt(state.joinedAt())
                .lastMessageAt(state.lastMessageAt())
                .build();
    }
}
