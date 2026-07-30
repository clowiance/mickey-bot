package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.Report;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;

    public void createReport(Long chatId, Long reporterId, String reporterUsername, Long targetId, String targetUsername, String reason) {
        Report report = Report.builder()
                .chatId(chatId)
                .reporterId(reporterId)
                .reporterUsername(reporterUsername)
                .targetId(targetId)
                .targetUsername(targetUsername)
                .reason(reason)
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
    }
}
