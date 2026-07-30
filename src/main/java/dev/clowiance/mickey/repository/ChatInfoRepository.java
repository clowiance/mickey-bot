package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ChatInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatInfoRepository extends JpaRepository<ChatInfo, Long> {
}
