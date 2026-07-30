package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ScheduledBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledBroadcastRepository extends JpaRepository<ScheduledBroadcast, Long> {

    List<ScheduledBroadcast> findBySentFalseAndSendAtBefore(LocalDateTime dateTime);
}
