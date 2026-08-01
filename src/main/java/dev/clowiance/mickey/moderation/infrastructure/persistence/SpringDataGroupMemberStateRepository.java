package dev.clowiance.mickey.moderation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataGroupMemberStateRepository
        extends JpaRepository<GroupMemberStateJpaEntity, GroupMemberStateId> {
    List<GroupMemberStateJpaEntity> findByIdChatId(Long chatId);
}
