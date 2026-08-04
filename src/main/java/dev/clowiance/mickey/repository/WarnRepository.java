package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.WarnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarnRepository extends JpaRepository<WarnEntity, Long> {
    List<WarnEntity> findByUserIdAndActiveTrue(Long userId);
    List<WarnEntity> findByChatIdAndUserIdAndActiveTrue(Long chatId, Long userId);
    List<WarnEntity> findByActiveTrue();
}
