package dev.clowiance.mickey.administration.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataManagedChatSettingsRepository
        extends JpaRepository<ManagedChatSettingsJpaEntity, Long> {
    List<ManagedChatSettingsJpaEntity> findAllByOrderByTitleAscChatIdAsc();
}
