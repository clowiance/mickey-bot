package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.Report;
import dev.clowiance.mickey.entity.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByChatIdOrderByCreatedAtDesc(Long chatId);

    List<Report> findByChatIdOrderByCreatedAtAsc(Long chatId);

    List<Report> findByChatIdAndStatusOrderByCreatedAtDesc(Long chatId, ReportStatus status);

    List<Report> findByChatIdAndStatusOrderByCreatedAtAsc(Long chatId, ReportStatus status);

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findAllByOrderByCreatedAtAsc();

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status);
}
