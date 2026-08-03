package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.Report;
<<<<<<< HEAD
<<<<<<< HEAD
import dev.clowiance.mickey.entity.enums.ReportStatus;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
import dev.clowiance.mickey.entity.enums.ReportStatus;
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    List<Report> findByChatIdOrderByCreatedAtDesc(Long chatId);

    List<Report> findByChatIdOrderByCreatedAtAsc(Long chatId);

    List<Report> findByChatIdAndStatusOrderByCreatedAtDesc(Long chatId, ReportStatus status);

    List<Report> findByChatIdAndStatusOrderByCreatedAtAsc(Long chatId, ReportStatus status);

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findAllByOrderByCreatedAtAsc();

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status);
<<<<<<< HEAD
=======
    List<Report> findByChatIdAndStatusOrderByCreatedAtDesc(Long chatId, String status);

    List<Report> findByStatusOrderByCreatedAtDesc(String status);

    long countByChatIdAndStatus(Long chatId, String status);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
}
