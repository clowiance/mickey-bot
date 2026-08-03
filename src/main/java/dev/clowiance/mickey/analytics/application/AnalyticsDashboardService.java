package dev.clowiance.mickey.analytics.application;

import dev.clowiance.mickey.administration.application.ChatAdministrationService;
import dev.clowiance.mickey.analytics.domain.ChartDefinition;
import dev.clowiance.mickey.analytics.domain.ChartPoint;
import dev.clowiance.mickey.analytics.domain.RenderedChart;
import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.entity.EconomyAccount;
import dev.clowiance.mickey.entity.enums.ReportStatus;
import dev.clowiance.mickey.moderation.application.GroupMemberService;
import dev.clowiance.mickey.moderation.domain.GroupMemberStatistics;
import dev.clowiance.mickey.service.ActivityAuditService;
import dev.clowiance.mickey.service.ChatMemberService;
import dev.clowiance.mickey.service.EconomyService;
import dev.clowiance.mickey.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AnalyticsDashboardService {

    private final ChartRendererPort renderer;
    private final GroupMemberService groupMemberService;
    private final ReportService reportService;
    private final EconomyService economyService;
    private final ChatMemberService chatMemberService;
    private final ActivityAuditService activityAuditService;
    private final ChatAdministrationService chatAdministrationService;
    private final UniversityCatalogService catalogService;

    public RenderedChart groupOverview(Long chatId) {
        GroupMemberStatistics stats = groupMemberService.statistics(chatId);
        long reports = reportService.list(chatId, ReportStatus.WAITING, true).size();
        List<ChartPoint> points = List.of(
                new ChartPoint("MEMBERS", stats.memberCount()),
                new ChartPoint("WARNINGS", stats.totalWarnings()),
                new ChartPoint("MUTED", stats.mutedCount()),
                new ChartPoint("BANNED", stats.bannedCount()),
                new ChartPoint("REPORTS", reports));
        return render("group-statistics.png", "📊 <b>Статистика групи</b>\nДані модерації та активні репорти.",
                new ChartDefinition("GROUP STATISTICS", "CURRENT DATABASE STATE", points));
    }

    public RenderedChart economyRating(boolean earned) {
        List<EconomyAccount> accounts = economyService.leaderboard(10);
        List<ChartPoint> points = new ArrayList<>();
        StringBuilder caption = new StringBuilder(earned
                ? "🏆 <b>Рейтинг за всіма заробленими кронами</b>\n"
                : "🏆 <b>Рейтинг за поточним балансом</b>\n");
        int place = 1;
        for (EconomyAccount account : accounts) {
            String label = resolveMemberLabel(account.getUserId());
            long value = earned ? account.getTotalEarned() : account.getBalance();
            points.add(new ChartPoint(label, value));
            caption.append(place++).append(". ").append(label).append(" — ").append(value).append(" крон\n");
        }
        return render("economy-rating.png", caption.toString(),
                new ChartDefinition(earned ? "TOTAL EARNED RATING" : "BALANCE RATING", "TOP 10 USERS", points));
    }

    public RenderedChart activityOverview(Long chatId) {
        var counts = activityAuditService.countByCategory(chatId, LocalDateTime.now().minusDays(30));
        List<ChartPoint> points = counts.entrySet().stream()
                .map(entry -> new ChartPoint(ascii(entry.getKey()), entry.getValue()))
                .toList();
        return render("activity-statistics.png",
                "📈 <b>Активність за 30 днів</b>\nКількість записів аудиту за категоріями.",
                new ChartDefinition("ACTIVITY BY CATEGORY", "LAST 30 DAYS", points));
    }

    public RenderedChart globalOverview() {
        var chats = chatAdministrationService.findAll();
        GroupMemberStatistics total = chats.stream()
                .map(chat -> groupMemberService.statistics(chat.chatId()))
                .reduce(new GroupMemberStatistics(0, 0, 0, 0), GroupMemberStatistics::plus);
        List<ChartPoint> points = List.of(
                new ChartPoint("GROUPS", chats.size()),
                new ChartPoint("PROGRAMS", catalogService.countActive()),
                new ChartPoint("MEMBERS", total.memberCount()),
                new ChartPoint("WARNINGS", total.totalWarnings()),
                new ChartPoint("REPORTS", reportService.list(null, ReportStatus.WAITING, true).size()));
        return render("global-statistics.png", "📊 <b>Глобальна статистика Mickey Bot</b>",
                new ChartDefinition("GLOBAL BOT STATISTICS", "ALL MANAGED GROUPS", points));
    }

    private RenderedChart render(String fileName, String caption, ChartDefinition definition) {
        return new RenderedChart(fileName, caption, renderer.render(definition));
    }

    private String resolveMemberLabel(Long userId) {
        var member = chatMemberService.findById(userId);
        if (member != null && member.getUsername() != null && !member.getUsername().isBlank()) {
            return "@" + ascii(member.getUsername());
        }
        return "ID_" + userId;
    }

    private String ascii(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_@.-]", "_");
        return normalized.length() <= 20 ? normalized : normalized.substring(0, 20);
    }
}
