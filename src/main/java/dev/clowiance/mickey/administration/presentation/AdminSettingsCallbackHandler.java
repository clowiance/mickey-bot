package dev.clowiance.mickey.administration.presentation;

import dev.clowiance.mickey.administration.application.ChatAdministrationService;
import dev.clowiance.mickey.administration.domain.ChatFeature;
import dev.clowiance.mickey.administration.domain.ManagedChatSettings;
import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import dev.clowiance.mickey.moderation.application.GroupMemberService;
import dev.clowiance.mickey.moderation.domain.GroupMemberStatistics;
import dev.clowiance.mickey.entity.enums.ReportStatus;
import dev.clowiance.mickey.service.ReportService;
import dev.clowiance.mickey.telegram.callback.CallbackContext;
import dev.clowiance.mickey.telegram.callback.CallbackHandler;
import dev.clowiance.mickey.telegram.callback.CallbackView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminSettingsCallbackHandler implements CallbackHandler {

    private static final int PAGE_SIZE = 6;

    private final ChatAdministrationService chatAdministrationService;
    private final UniversityCatalogService catalogService;
    private final GroupMemberService groupMemberService;
    private final ReportService reportService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData.equals("admin:settings")
                || callbackData.startsWith("admin:toggle:")
                || callbackData.equals("admin:audience")
                || callbackData.startsWith("admin:specialty:")
                || callbackData.equals("dev:groups")
                || callbackData.equals("dev:catalog")
                || callbackData.equals("admin:stats")
                || callbackData.equals("dev:stats");
    }

    @Override
    public CallbackView handle(CallbackContext context) {
        if (context.data().startsWith("dev:") && !context.developer()) {
            return forbidden();
        }
        if (context.data().startsWith("admin:") && !context.developer() && !context.chatAdmin()) {
            return forbidden();
        }

        String data = context.data();
        if ("dev:groups".equals(data)) {
            return new CallbackView(renderGroups(), singleBack("dev:panel"));
        }
        if ("dev:catalog".equals(data)) {
            return new CallbackView(renderCatalogOverview(), singleBack("dev:panel"));
        }
        if ("dev:stats".equals(data)) {
            return new CallbackView(renderDeveloperStatistics(), singleBack("dev:panel"));
        }
        if ("admin:stats".equals(data)) {
            return new CallbackView(renderChatStatistics(context.chatId()), singleBack("admin:panel"));
        }
        if ("admin:toggle:welcome".equals(data)) {
            chatAdministrationService.toggle(context.chatId(), ChatFeature.WELCOME);
        } else if ("admin:toggle:moderation".equals(data)) {
            chatAdministrationService.toggle(context.chatId(), ChatFeature.MODERATION);
        } else if ("admin:toggle:broadcasts".equals(data)) {
            chatAdministrationService.toggle(context.chatId(), ChatFeature.BROADCASTS);
        } else if ("admin:audience".equals(data)) {
            chatAdministrationService.cycleAudience(context.chatId());
        } else if (data.startsWith("admin:specialty:set:")) {
            Long specialtyId = parseLong(data.substring("admin:specialty:set:".length()));
            if (specialtyId != null) {
                chatAdministrationService.assignSpecialty(context.chatId(), specialtyId);
            }
        } else if ("admin:specialty:clear".equals(data)) {
            chatAdministrationService.assignSpecialty(context.chatId(), null);
        } else if (data.startsWith("admin:specialty:page:")) {
            int page = parsePage(data.substring("admin:specialty:page:".length()));
            return specialtyPage(context.chatId(), page);
        }

        ManagedChatSettings settings = chatAdministrationService.getOrCreate(context.chatId());
        return new CallbackView(renderSettings(settings), settingsKeyboard(settings));
    }

    private CallbackView specialtyPage(Long chatId, int requestedPage) {
        List<Specialty> specialties = new ArrayList<>();
        specialties.addAll(catalogService.findActive(EducationLevel.BACHELOR));
        specialties.addAll(catalogService.findActive(EducationLevel.MASTER));
        specialties.sort(Comparator.comparing(Specialty::educationLevel).thenComparingInt(Specialty::displayOrder));

        int totalPages = Math.max(1, (specialties.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, specialties.size());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Specialty specialty : specialties.subList(from, to)) {
            String prefix = specialty.educationLevel() == EducationLevel.BACHELOR ? "Б" : "М";
            rows.add(List.of(button(shorten(prefix + " · " + specialty.code() + " " + specialty.name()),
                    "admin:specialty:set:" + specialty.id())));
        }
        List<InlineKeyboardButton> navigation = new ArrayList<>();
        if (page > 0) {
            navigation.add(button("⬅️", "admin:specialty:page:" + (page - 1)));
        }
        navigation.add(button((page + 1) + "/" + totalPages, "admin:specialty:page:" + page));
        if (page + 1 < totalPages) {
            navigation.add(button("➡️", "admin:specialty:page:" + (page + 1)));
        }
        rows.add(navigation);
        rows.add(List.of(button("🧹 Відв'язати спеціальність", "admin:specialty:clear")));
        rows.add(List.of(button("⬅️ До налаштувань", "admin:settings")));

        String text = "🎓 <b>Спеціальність групи</b>\n\n"
                + "Оберіть напрям, до якого належить ця Telegram-група. "
                + "Це впливатиме на привітання й подальші цільові розсилки.";
        return new CallbackView(text, InlineKeyboardMarkup.builder().keyboard(rows).build());
    }

    private String renderSettings(ManagedChatSettings settings) {
        String specialty = settings.specialtyId() == null
                ? "не призначена"
                : catalogService.findById(settings.specialtyId())
                .map(value -> value.code() + " " + value.name() + " (" + value.educationLevel().getDisplayName() + ")")
                .orElse("видалена з каталогу");
        return "⚙️ <b>Налаштування групи</b>\n\n"
                + "💬 " + escape(settings.title() == null ? String.valueOf(settings.chatId()) : settings.title()) + "\n"
                + "👥 Аудиторія: <b>" + settings.audience().getDisplayName() + "</b>\n"
                + "🎓 Спеціальність: <b>" + escape(specialty) + "</b>\n\n"
                + state("Привітання", settings.welcomeEnabled()) + "\n"
                + state("Модерація", settings.moderationEnabled()) + "\n"
                + state("Розсилки", settings.broadcastsEnabled());
    }

    private InlineKeyboardMarkup settingsKeyboard(ManagedChatSettings settings) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button(toggleLabel("👋 Привітання", settings.welcomeEnabled()), "admin:toggle:welcome"),
                        button(toggleLabel("🛡️ Модерація", settings.moderationEnabled()), "admin:toggle:moderation")))
                .keyboardRow(List.of(button(toggleLabel("📣 Розсилки", settings.broadcastsEnabled()),
                        "admin:toggle:broadcasts")))
                .keyboardRow(List.of(button("👥 Змінити аудиторію", "admin:audience")))
                .keyboardRow(List.of(button("🎓 Обрати спеціальність", "admin:specialty:page:0")))
                .keyboardRow(List.of(button("⬅️ До адмін-панелі", "admin:panel")))
                .build();
    }

    private String renderGroups() {
        List<ManagedChatSettings> chats = chatAdministrationService.findAll();
        if (chats.isEmpty()) {
            return "🧩 Керованих груп ще немає.";
        }
        StringBuilder text = new StringBuilder("🧩 <b>Керовані групи</b>\n\n");
        chats.stream().limit(30).forEach(chat -> text.append("• <code>").append(chat.chatId())
                .append("</code> — ").append(escape(chat.title() == null ? "без назви" : chat.title()))
                .append("\n  ").append(chat.audience().getDisplayName())
                .append(" | ").append(chat.specialtyId() == null ? "без спеціальності" : "specialty #" + chat.specialtyId())
                .append("\n"));
        if (chats.size() > 30) {
            text.append("\nПоказано перші 30 із ").append(chats.size()).append(" груп.");
        }
        return text.toString();
    }

    private String renderCatalogOverview() {
        return "🎓 <b>Каталог УКД</b>\n\n"
                + "Активних позицій: <b>" + catalogService.countActive() + "</b>\n"
                + "Бакалаврат: " + catalogService.findAll(EducationLevel.BACHELOR).size() + "\n"
                + "Магістратура: " + catalogService.findAll(EducationLevel.MASTER).size() + "\n\n"
                + "Керування даними:\n"
                + "<code>/catalogadmin list bachelor</code>\n"
                + "<code>/catalogadmin price ID full 50600</code>\n"
                + "<code>/catalogadmin price ID part none</code>\n"
                + "<code>/catalogadmin active ID</code>";
    }

    private String renderChatStatistics(Long chatId) {
        GroupMemberStatistics statistics = groupMemberService.statistics(chatId);
        int pendingReports = reportService.list(chatId, ReportStatus.WAITING, true).size();
        return "📊 <b>Статистика групи</b>\n\n"
                + "👥 Учасників у базі: <b>" + statistics.memberCount() + "</b>\n"
                + "⚠️ Попереджень: <b>" + statistics.totalWarnings() + "</b>\n"
                + "🔇 Замучених: <b>" + statistics.mutedCount() + "</b>\n"
                + "⛔ Забанених: <b>" + statistics.bannedCount() + "</b>\n"
                + "🚨 Активних репортів: <b>" + pendingReports + "</b>";
    }

    private String renderDeveloperStatistics() {
        var chats = chatAdministrationService.findAll();
        GroupMemberStatistics total = new GroupMemberStatistics(0, 0, 0, 0);
        for (ManagedChatSettings chat : chats) {
            total = total.plus(groupMemberService.statistics(chat.chatId()));
        }
        return "📊 <b>Глобальна статистика бота</b>\n\n"
                + "🧩 Керованих груп: <b>" + chats.size() + "</b>\n"
                + "🎓 Активних програм: <b>" + catalogService.countActive() + "</b>\n"
                + "👥 Записів учасників: <b>" + total.memberCount() + "</b>\n"
                + "⚠️ Попереджень: <b>" + total.totalWarnings() + "</b>\n"
                + "🚨 Активних репортів: <b>" + reportService.list(null, ReportStatus.WAITING, true).size() + "</b>";
    }

    private CallbackView forbidden() {
        return new CallbackView("🔒 Недостатньо прав для цієї дії.", null);
    }

    private InlineKeyboardMarkup singleBack(String callbackData) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⬅️ Назад", callbackData)))
                .build();
    }

    private String state(String title, boolean enabled) {
        return (enabled ? "✅ " : "⛔ ") + title;
    }

    private String toggleLabel(String title, boolean enabled) {
        return (enabled ? "✅ " : "⛔ ") + title;
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int parsePage(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String shorten(String value) {
        return value.length() <= 48 ? value : value.substring(0, 45) + "…";
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
