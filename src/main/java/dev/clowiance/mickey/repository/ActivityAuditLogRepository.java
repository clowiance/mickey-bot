package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ActivityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityAuditLogRepository extends JpaRepository<ActivityAuditLog, Long>, JpaSpecificationExecutor<ActivityAuditLog> {
}
