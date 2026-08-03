package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ScheduledBroadcast;
import dev.clowiance.mickey.entity.BroadcastStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledBroadcastRepository extends JpaRepository<ScheduledBroadcast, Long> {

    long countByStatus(BroadcastStatus status);

    List<ScheduledBroadcast> findTop100ByOrderBySendAtDesc();

    @Query("""
            select b.id from ScheduledBroadcast b
            where b.sent = false
              and b.sendAt <= :now
              and (b.status is null or b.status in :statuses)
              and (b.nextAttemptAt is null or b.nextAttemptAt <= :now)
            order by b.sendAt asc
            """)
    List<Long> findDueIds(@Param("now") LocalDateTime now,
                          @Param("statuses") List<BroadcastStatus> statuses,
                          Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ScheduledBroadcast b
               set b.status = :processing,
                   b.processingStartedAt = :now,
                   b.lastAttemptAt = :now
             where b.id = :id
               and b.sent = false
               and (b.status is null or b.status in :claimable)
               and (b.nextAttemptAt is null or b.nextAttemptAt <= :now)
            """)
    int claim(@Param("id") Long id,
              @Param("now") LocalDateTime now,
              @Param("processing") BroadcastStatus processing,
              @Param("claimable") List<BroadcastStatus> claimable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ScheduledBroadcast b
               set b.status = :failed,
                   b.processingStartedAt = null,
                   b.nextAttemptAt = :retryAt,
                   b.lastError = :reason
             where b.status = :processing and b.processingStartedAt < :cutoff
            """)
    int recoverStale(@Param("processing") BroadcastStatus processing,
                     @Param("failed") BroadcastStatus failed,
                     @Param("cutoff") LocalDateTime cutoff,
                     @Param("retryAt") LocalDateTime retryAt,
                     @Param("reason") String reason);
}
