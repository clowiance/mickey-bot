package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.PunishmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PunishmentLogRepository extends JpaRepository<PunishmentLog, Long> {

    List<PunishmentLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
