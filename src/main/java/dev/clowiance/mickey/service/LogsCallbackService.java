package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ActivityAuditLog;
import dev.clowiance.mickey.helper.KeyboardHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogsCallbackService {

    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final ActivityAuditService activityAuditService;
    private final PermissionService permissionService;
    private final KeyboardHelper keyboardHelper;

    public void handleCallback(TelegramLongPollingBot bot, CallbackQuery query, String data) {
        Long chatId = query.getMessage().getChatId();
        Integer messageId = query.getMessage().getMessageId();
        Long userId = query.getFrom().getId();

        if (!permissionService.isUserChatAdmin(bot, chatId, userId)) {
            answer(bot, query, "🔒 У тебе немає доступу до логів цього чату.", true);
            return;
        }

        if ("logs_close".equals(data)) {
            handleClose(bot, query, chatId, messageId);
            return;
        }

        if (data.startsWith("logs_confirm:")) {
            handleConfirm(bot, query, data, chatId, messageId);
            return;
        }

        if (data.startsWith("logs_filter:")) {
            handleFilterOrPage(bot, query, data, chatId, messageId);
            return;
        }

        if (data.startsWith("logs_page:")) {
            handleFilterOrPage(bot, query, data, chatId, messageId);
            return;
        }

        answer(bot, query, null, false);
    }

    private void handleClose(TelegramLongPollingBot bot, CallbackQuery query, Long chatId, Integer messageId) {
        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setText("❌ Перегляд логів закрито адміністратором.");
            edit.setReplyMarkup(null);
            bot.execute(edit);
        } catch (TelegramApiException e) {
            log.error("не вдалося закрити панель логів", e);
        }
        answer(bot, query, "Панель логів закрита", false);
    }

    private void handleConfirm(TelegramLongPollingBot bot, CallbackQuery query, String data, Long chatId, Integer messageId) {
        Long targetChatId;
        try {
            targetChatId = Long.parseLong(data.substring("logs_confirm:".length()));
        } catch (NumberFormatException e) {
            answer(bot, query, "⚠️ Некоректний запит.", true);
            return;
        }
        renderAndEdit(bot, query, chatId, messageId, targetChatId, "ALL", 0);
    }

    private void handleFilterOrPage(TelegramLongPollingBot bot, CallbackQuery query, String data, Long chatId, Integer messageId) {
        String[] parts = data.split(":");
        if (parts.length < 4) {
            answer(bot, query, "⚠️ Некоректний запит.", true);
            return;
        }
        Long targetChatId;
        int page;
        try {
            targetChatId = Long.parseLong(parts[1]);
            page = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            answer(bot, query, "⚠️ Некоректний запит.", true);
            return;
        }
        String category = parts[2];
        renderAndEdit(bot, query, chatId, messageId, targetChatId, category, Math.max(0, page));
    }

    private void renderAndEdit(TelegramLongPollingBot bot, CallbackQuery query, Long chatId, Integer messageId,
                                Long targetChatId, String category, int page) {
        ActivityAuditService.LogsPage logsPage = activityAuditService.queryLogsPaged(targetChatId, category, page, PAGE_SIZE);

        if (logsPage.content().isEmpty() && page > 0) {
            logsPage = activityAuditService.queryLogsPaged(targetChatId, category, page - 1, PAGE_SIZE);
        }

        StringBuilder sb = new StringBuilder("📋 <b>Звіт логів (Фільтр: ").append(categoryLabel(category))
                .append(" | Сторінка ").append(logsPage.page() + 1).append("):</b>\n\n");
        if (logsPage.content().isEmpty()) {
            sb.append("<i>Зафіксованих подій за цим фільтром не знайдено.</i>");
        } else {
            for (ActivityAuditLog entry : logsPage.content()) {
                sb.append("• <code>")
                        .append(entry.getCreatedAt() != null ? entry.getCreatedAt().format(FORMATTER) : "-")
                        .append("</code> | ")
                        .append("<b>[").append(entry.getActionCategory()).append("]</b> ")
                        .append(entry.getActionType()).append(" | ")
                        .append("Користувач: <b>")
                        .append(entry.getUsername() != null ? "@" + escapeHtml(entry.getUsername()) : entry.getUserId())
                        .append("</b>");
                if (entry.getDetails() != null && !entry.getDetails().isBlank()) {
                    sb.append("\n  └ <i>").append(escapeHtml(entry.getDetails())).append("</i>");
                }
                sb.append("\n\n");
            }
        }

        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setParseMode("HTML");
            edit.setText(sb.toString());
            edit.setReplyMarkup(keyboardHelper.logsMenu(targetChatId, category, logsPage.page(), logsPage.hasNext(), logsPage.hasPrev()));
            bot.execute(edit);
        } catch (TelegramApiRequestException e) {
            if (e.getApiResponse() != null && e.getApiResponse().contains("message is not modified")) {
                log.debug("панель логів не змінилася при оновленні: {}", e.getMessage());
            } else {
                log.error("не вдалося оновити панель логів", e);
            }
        } catch (TelegramApiException e) {
            log.error("не вдалося оновити панель логів", e);
        }
        answer(bot, query, null, false);
    }

    private String categoryLabel(String category) {
        return switch (category) {
            case "MESSAGE" -> "Повідомлення";
            case "MODERATION" -> "Модерація";
            default -> "Всі події";
        };
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void answer(TelegramLongPollingBot bot, CallbackQuery query, String text, boolean alert) {
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(query.getId());
            if (text != null) {
                answer.setText(text);
                answer.setShowAlert(alert);
            }
            bot.execute(answer);
        } catch (TelegramApiException ignored) {
        }
    }
}
