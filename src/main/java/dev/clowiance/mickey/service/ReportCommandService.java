package dev.clowiance.mickey.service;

import dev.clowiance.mickey.config.BotProperties;
import dev.clowiance.mickey.entity.Report;
import dev.clowiance.mickey.entity.enums.ReportStatus;
import dev.clowiance.mickey.helper.TargetHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportCommandService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int PAGE_SIZE = 1;
    private static final String ALL_SCOPE = "0";

    private final ReportService reportService;
    private final TargetHelper targetHelper;
    private final PermissionService permissionService;
    private final BotProperties botProperties;
    private final ReportNotificationService notificationService;
    private final ActivityAuditService activityAuditService;

    public void handleCreate(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        if (target.remainingArgs() == null || target.remainingArgs().isBlank()) {
            reply(bot, message, TextHelper.REPORT_REASON_REQUIRED);
            return;
        }
        if (target.id() != null && target.id().equals(message.getFrom().getId())) {
            reply(bot, message, TextHelper.REPORT_SELF);
            return;
        }

        String chatTitle = message.getChat().getTitle();
        String chatUsername = message.getChat().getUserName();

        Report report = reportService.create(
                message.getChatId(),
                chatTitle,
                chatUsername,
                message.getFrom().getId(),
                message.getFrom().getUserName(),
                target.id(),
                target.username() != null ? target.username() : target.firstName(),
                target.remainingArgs().trim()
        );

        reply(bot, message, String.format(TextHelper.REPORT_CREATED, report.getId()));
        activityAuditService.logActivity(message.getChatId(), message.getFrom().getId(), message.getFrom().getUserName(),
                "MODERATION", "REPORT_CREATED",
                String.format("Скарга #%d на %s | Причина: %s", report.getId(),
                        mention(report.getTargetUsername(), report.getTargetId()), report.getReason()));
        notificationService.notifyAdmins(bot, report);
    }

    public void handleListCommand(TelegramLongPollingBot bot, Message message, boolean isDev) {
        boolean privateChat = message.getChat().isUserChat();
        String scope = privateChat ? ALL_SCOPE : String.valueOf(message.getChatId());
        RenderResult result = render(scope, "n", "w", 0);
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(result.text());
            sendMessage.setReplyMarkup(result.keyboard());
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("не вдалося надіслати список скарг", e);
        }
    }

    public void handleCallback(TelegramLongPollingBot bot, CallbackQuery callbackQuery, String data) {
        String[] parts = data.split(":");
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long userId = callbackQuery.getFrom().getId();
        boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(userId);

        try {
            if (parts.length < 2) {
                return;
            }
            String op = parts[1];

            if ("L".equals(op) && parts.length == 6) {
                String scope = parts[2];
                String sort = parts[3];
                String filter = parts[4];
                int page = safeInt(parts[5]);

                if (!hasScopeAccess(bot, scope, userId, isDev)) {
                    answer(bot, callbackQuery, TextHelper.NO_REPORTS_ACCESS);
                    return;
                }
                RenderResult result = render(scope, sort, filter, page);
                editMessage(bot, chatId, messageId, result);
                answer(bot, callbackQuery, null);
                return;
            }

            if (("A".equals(op) || "D".equals(op)) && parts.length == 7) {
                Long reportId = Long.valueOf(parts[2]);
                String scope = parts[3];
                String sort = parts[4];
                String filter = parts[5];
                int page = safeInt(parts[6]);

                Optional<Report> reportOpt = reportService.findById(reportId);
                if (reportOpt.isEmpty()) {
                    answer(bot, callbackQuery, TextHelper.REPORT_NOT_FOUND);
                    return;
                }
                Report report = reportOpt.get();
                if (!isDev && !permissionService.isUserChatAdmin(bot, report.getChatId(), userId)) {
                    answer(bot, callbackQuery, TextHelper.NO_REPORTS_ACCESS);
                    return;
                }
                if (report.getStatus() != ReportStatus.WAITING) {
                    answer(bot, callbackQuery, String.format(TextHelper.REPORT_ALREADY_RESOLVED, report.getStatus()));
                    return;
                }

                String resolverUsername = callbackQuery.getFrom().getUserName();
                Optional<Report> updated = "A".equals(op)
                        ? reportService.accept(reportId, userId, resolverUsername)
                        : reportService.decline(reportId, userId, resolverUsername);

                if (updated.isEmpty()) {
                    answer(bot, callbackQuery, TextHelper.REPORT_NOT_FOUND);
                    return;
                }

                activityAuditService.logActivity(report.getChatId(), userId, resolverUsername, "MODERATION",
                        "A".equals(op) ? "REPORT_ACCEPTED" : "REPORT_DECLINED",
                        String.format("Скарга #%d на %s", reportId, mention(report.getTargetUsername(), report.getTargetId())));

                RenderResult result = render(scope, sort, filter, page);
                editMessage(bot, chatId, messageId, result);
                answer(bot, callbackQuery, "A".equals(op) ? TextHelper.REPORT_ACCEPTED_TOAST : TextHelper.REPORT_DECLINED_TOAST);
            }
        } catch (Exception e) {
            log.error("помилка обробки колбеку скарг", e);
        }
    }

    private boolean hasScopeAccess(TelegramLongPollingBot bot, String scope, Long userId, boolean isDev) {
        if (ALL_SCOPE.equals(scope)) {
            return isDev;
        }
        if (isDev) {
            return true;
        }
        Long chatId = Long.valueOf(scope);
        return permissionService.isUserChatAdmin(bot, chatId, userId);
    }

    private RenderResult render(String scope, String sort, String filter, int page) {
        Long chatIdScope = ALL_SCOPE.equals(scope) ? null : Long.valueOf(scope);
        ReportStatus statusFilter = parseStatus(filter);
        boolean newestFirst = "n".equals(sort);

        List<Report> all = reportService.list(chatIdScope, statusFilter, newestFirst);
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));
        int from = clampedPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        List<Report> pageItems = from < to ? all.subList(from, to) : List.of();

        StringBuilder sb = new StringBuilder();
        sb.append("🚩 <b>Скарги</b>");
        sb.append(ALL_SCOPE.equals(scope) ? " (усі чати)" : " (цей чат)").append("\n");
        sb.append("Фільтр: ").append(filterLabel(filter)).append(" | Сортування: ")
                .append("n".equals(sort) ? "спочатку нові" : "спочатку старі").append("\n");
        sb.append("Знайдено: ").append(all.size()).append(" | Сторінка ").append(clampedPage + 1)
                .append("/").append(totalPages).append("\n\n");

        if (pageItems.isEmpty()) {
            sb.append(TextHelper.NO_REPORTS_FOUND);
        } else {
            Report r = pageItems.get(0);
            sb.append("🆔 <b>Скарга #").append(r.getId()).append("</b> [").append(statusLabel(r.getStatus())).append("]\n");
            sb.append("🕒 ").append(r.getCreatedAt() != null ? r.getCreatedAt().format(FORMATTER) : "-").append("\n");
            sb.append("💬 Чат: ").append(r.getChatTitle() != null ? r.getChatTitle() : "-")
                    .append(" (ID: ").append(r.getChatId()).append(")\n");
            sb.append("🔗 ").append(ReportService.buildChatLink(r.getChatId(), r.getChatUsername())).append("\n");
            sb.append("👤 На кого: ").append(mention(r.getTargetUsername(), r.getTargetId())).append("\n");
            sb.append("✍️ Від: ").append(mention(r.getReporterUsername(), r.getReporterId())).append("\n");
            sb.append("📝 Причина: ").append(r.getReason()).append("\n");
            if (r.getStatus() != ReportStatus.WAITING && r.getResolvedAt() != null) {
                sb.append("👮 Розглянув: ").append(mention(r.getResolvedByUsername(), r.getResolvedById()))
                        .append(" (").append(r.getResolvedAt().format(FORMATTER)).append(")\n");
            }
        }

        InlineKeyboardMarkup keyboard = buildKeyboard(pageItems, scope, sort, filter, clampedPage, totalPages);
        return new RenderResult(sb.toString(), keyboard);
    }

    private InlineKeyboardMarkup buildKeyboard(List<Report> pageItems, String scope, String sort, String filter,
                                               int page, int totalPages) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        if (!pageItems.isEmpty()) {
            Report r = pageItems.get(0);
            if (r.getStatus() == ReportStatus.WAITING) {
                InlineKeyboardButton accept = InlineKeyboardButton.builder()
                        .text("✅ Прийняти")
                        .callbackData(listCallback("A", r.getId(), scope, sort, filter, page))
                        .build();
                InlineKeyboardButton decline = InlineKeyboardButton.builder()
                        .text("❌ Відхилити")
                        .callbackData(listCallback("D", r.getId(), scope, sort, filter, page))
                        .build();
                rows.add(List.of(accept, decline));
            }
        }

        if (totalPages > 1) {
            List<InlineKeyboardButton> paginationRow = new ArrayList<>();
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("⏮")
                    .callbackData(listCallback("L", null, scope, sort, filter, 0))
                    .build());
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("◀️")
                    .callbackData(listCallback("L", null, scope, sort, filter, Math.max(0, page - 1)))
                    .build());
            paginationRow.add(InlineKeyboardButton.builder()
                    .text((page + 1) + "/" + totalPages)
                    .callbackData(listCallback("L", null, scope, sort, filter, page))
                    .build());
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("▶️")
                    .callbackData(listCallback("L", null, scope, sort, filter, Math.min(totalPages - 1, page + 1)))
                    .build());
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("⏭")
                    .callbackData(listCallback("L", null, scope, sort, filter, totalPages - 1))
                    .build());
            rows.add(paginationRow);
        }

        String nextSort = "n".equals(sort) ? "o" : "n";
        InlineKeyboardButton sortButton = InlineKeyboardButton.builder()
                .text("n".equals(sort) ? "🔀 Спочатку старі" : "🔀 Спочатку нові")
                .callbackData(listCallback("L", null, scope, nextSort, filter, 0))
                .build();
        rows.add(List.of(sortButton));

        rows.add(List.of(
                filterButton("Очікують", "w", scope, sort, filter),
                filterButton("Прийняті", "a", scope, sort, filter),
                filterButton("Відхилені", "d", scope, sort, filter),
                filterButton("Усі", "*", scope, sort, filter)
        ));

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private InlineKeyboardButton filterButton(String label, String filterValue, String scope, String sort, String currentFilter) {
        boolean active = currentFilter.equals(filterValue);
        String prefix = active ? "📂 " : "📁 ";
        return InlineKeyboardButton.builder()
                .text(prefix + label)
                .callbackData(listCallback("L", null, scope, sort, filterValue, 0))
                .build();
    }

    private String listCallback(String op, Long reportId, String scope, String sort, String filter, int page) {
        if (reportId != null) {
            return "R:" + op + ":" + reportId + ":" + scope + ":" + sort + ":" + filter + ":" + page;
        }
        return "R:" + op + ":" + scope + ":" + sort + ":" + filter + ":" + page;
    }

    private ReportStatus parseStatus(String filter) {
        return switch (filter) {
            case "w" -> ReportStatus.WAITING;
            case "a" -> ReportStatus.ACCEPTED;
            case "d" -> ReportStatus.DECLINED;
            default -> null;
        };
    }

    private String filterLabel(String filter) {
        return switch (filter) {
            case "w" -> "очікують";
            case "a" -> "прийняті";
            case "d" -> "відхилені";
            default -> "усі";
        };
    }

    private String statusLabel(ReportStatus status) {
        return switch (status) {
            case WAITING -> "WAITING";
            case ACCEPTED -> "ACCEPTED";
            case DECLINED -> "DECLINED";
        };
    }

    private String mention(String username, Long id) {
        if (username != null && !username.isBlank()) {
            return "@" + username;
        }
        return id != null ? String.valueOf(id) : "невідомо";
    }

    private int safeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void editMessage(TelegramLongPollingBot bot, Long chatId, Integer messageId, RenderResult result) {
        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setText(result.text());
            edit.setParseMode("HTML");
            edit.setReplyMarkup(result.keyboard());
            bot.execute(edit);
        } catch (TelegramApiRequestException e) {
            if (e.getApiResponse() != null && e.getApiResponse().contains("message is not modified")) {
                log.debug("Повідомлення не змінилося при редагуванні: {}", e.getMessage());
            } else {
                log.error("не вдалося оновити список скарг", e);
            }
        } catch (TelegramApiException e) {
            log.error("не вдалося оновити список скарг", e);
        }
    }

    private void answer(TelegramLongPollingBot bot, CallbackQuery callbackQuery, String text) {
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            if (text != null) {
                answer.setText(text);
            }
            bot.execute(answer);
        } catch (TelegramApiException ignored) {
        }
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }

    private record RenderResult(String text, InlineKeyboardMarkup keyboard) {
    }
}
