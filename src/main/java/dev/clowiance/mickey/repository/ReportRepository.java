package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByChatIdAndStatusOrderByCreatedAtDesc(Long chatId, String status);

    List<Report> findByStatusOrderByCreatedAtDesc(String status);

    long countByChatIdAndStatus(Long chatId, String status);
}
