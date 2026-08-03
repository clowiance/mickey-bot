package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.Report;
<<<<<<< HEAD
import dev.clowiance.mickey.entity.enums.ReportStatus;
=======
import dev.clowiance.mickey.helper.TextHelper;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
import dev.clowiance.mickey.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
<<<<<<< HEAD
import java.util.Optional;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;

<<<<<<< HEAD
    public Report create(Long chatId, String chatTitle, String chatUsername, Long reporterId,
                         String reporterUsername, Long targetId, String targetUsername, String reason) {
        Report report = Report.builder()
                .chatId(chatId)
                .chatTitle(chatTitle)
                .chatUsername(chatUsername)
=======
    public void createReport(Long chatId, Long reporterId, String reporterUsername, Long targetId, String targetUsername, String reason) {
        Report report = Report.builder()
                .chatId(chatId)
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
                .reporterId(reporterId)
                .reporterUsername(reporterUsername)
                .targetId(targetId)
                .targetUsername(targetUsername)
                .reason(reason)
<<<<<<< HEAD
                .status(ReportStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();
        return repository.save(report);
    }

    public List<Report> list(Long chatIdScope, ReportStatus status, boolean newestFirst) {
        if (chatIdScope != null) {
            if (status != null) {
                return newestFirst
                        ? repository.findByChatIdAndStatusOrderByCreatedAtDesc(chatIdScope, status)
                        : repository.findByChatIdAndStatusOrderByCreatedAtAsc(chatIdScope, status);
            }
            return newestFirst
                    ? repository.findByChatIdOrderByCreatedAtDesc(chatIdScope)
                    : repository.findByChatIdOrderByCreatedAtAsc(chatIdScope);
        }
        if (status != null) {
            return newestFirst
                    ? repository.findByStatusOrderByCreatedAtDesc(status)
                    : repository.findByStatusOrderByCreatedAtAsc(status);
        }
        return newestFirst ? repository.findAllByOrderByCreatedAtDesc() : repository.findAllByOrderByCreatedAtAsc();
    }

    public Optional<Report> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Report> accept(Long id, Long resolverId, String resolverUsername) {
        return resolve(id, resolverId, resolverUsername, ReportStatus.ACCEPTED);
    }

    public Optional<Report> decline(Long id, Long resolverId, String resolverUsername) {
        return resolve(id, resolverId, resolverUsername, ReportStatus.DECLINED);
    }

    private Optional<Report> resolve(Long id, Long resolverId, String resolverUsername, ReportStatus newStatus) {
        Optional<Report> found = repository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Report report = found.get();
        if (report.getStatus() != ReportStatus.WAITING) {
            return Optional.of(report);
        }
        report.setStatus(newStatus);
        report.setResolvedById(resolverId);
        report.setResolvedByUsername(resolverUsername);
        report.setResolvedAt(LocalDateTime.now());
        return Optional.of(repository.save(report));
    }

    public static String buildChatLink(Long chatId, String chatUsername) {
        if (chatUsername != null && !chatUsername.isBlank()) {
            return "https://t.me/" + chatUsername;
        }
        if (chatId == null) {
            return "недоступне";
        }
        String idStr = String.valueOf(chatId);
        if (idStr.startsWith("-100")) {
            String internal = idStr.substring(4);
            return "https://t.me/c/" + internal + "/1";
        }
        return "недоступне (приватна група без публічного посилання)";
=======
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(report);
    }

    public List<Report> getPendingReportsByChat(Long chatId) {
        return repository.findByChatIdAndStatusOrderByCreatedAtDesc(chatId, "PENDING");
    }

    public List<Report> getAllPendingReports() {
        return repository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public void resolveReport(Long id) {
        repository.findById(id).ifPresent(r -> {
            r.setStatus("RESOLVED");
            repository.save(r);
        });
    }

    public String formatChatPendingReports(Long chatId) {
        List<Report> reports = getPendingReportsByChat(chatId);
        if (reports.isEmpty()) {
            return TextHelper.REPORTS_EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📋 <b>Репорти чату</b>\n\n");
        for (Report r : reports.stream().limit(10).toList()) {
            sb.append("• #").append(r.getId()).append(" ");
            if (r.getTargetUsername() != null) sb.append("@").append(r.getTargetUsername());
            else if (r.getTargetId() != null) sb.append("ID:").append(r.getTargetId());
            else sb.append("невідомий");
            sb.append(" - ").append(r.getReason()).append("\n");
        }
        return sb.toString();
    }

    public String formatAllPendingReports() {
        List<Report> reports = getAllPendingReports();
        if (reports.isEmpty()) {
            return TextHelper.REPORTS_EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📋 <b>Всі репорти</b>\n\n");
        for (Report r : reports.stream().limit(10).toList()) {
            sb.append("• #").append(r.getId()).append(" | Чат ").append(r.getChatId()).append(" | ");
            if (r.getTargetUsername() != null) sb.append("@").append(r.getTargetUsername());
            else if (r.getTargetId() != null) sb.append("ID:").append(r.getTargetId());
            else sb.append("невідомий");
            sb.append(" - ").append(r.getReason()).append("\n");
        }
        return sb.toString();
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    }
}
