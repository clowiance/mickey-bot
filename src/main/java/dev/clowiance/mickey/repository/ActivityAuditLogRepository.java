package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ActivityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityAuditLogRepository extends JpaRepository<ActivityAuditLog, Long>, JpaSpecificationExecutor<ActivityAuditLog> {

    long deleteByCreatedAtBefore(LocalDateTime cutoff);

    @Query("""
            select log.actionCategory, count(log)
            from ActivityAuditLog log
            where (:chatId is null or log.chatId = :chatId)
              and log.createdAt >= :after
            group by log.actionCategory
            order by count(log) desc
            """)
    List<Object[]> countByCategory(@Param("chatId") Long chatId, @Param("after") LocalDateTime after);
}
