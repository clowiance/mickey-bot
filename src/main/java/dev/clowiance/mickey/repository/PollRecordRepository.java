package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.PollRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRecordRepository extends JpaRepository<PollRecord, String> {
}
