package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.community.application.StudyGroupDirectory;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import dev.clowiance.mickey.helper.TelegramHtml;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupLinksAdminCommand implements ICommand {
    private final StudyGroupDirectory directory;

    @Override
    public String getCommand() {
        return "/groupadmin";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isDevOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        String input = args == null ? "" : args.trim();
        try {
            if (input.isBlank() || input.equalsIgnoreCase("list")) {
                list(bot, message.getChatId());
                return;
            }
            if (input.startsWith("enable ") || input.startsWith("disable ")) {
                boolean active = input.startsWith("enable ");
                Long id = Long.valueOf(input.substring(input.indexOf(' ') + 1).trim());
                StudyGroup group = directory.setActive(id, active);
                send(bot, message.getChatId(), (active ? "✅ Увімкнено: " : "⛔ Вимкнено: ")
                        + TelegramHtml.escape(group.specialty()));
                return;
            }
            if (input.startsWith("add ") || input.startsWith("update ")) {
                upsert(bot, message.getChatId(), input);
                return;
            }
            send(bot, message.getChatId(), usage());
        } catch (IllegalArgumentException exception) {
            send(bot, message.getChatId(), "❌ " + TelegramHtml.escape(exception.getMessage()) + "\n\n" + usage());
        }
    }

    private void upsert(TelegramLongPollingBot bot, Long chatId, String input) {
        boolean update = input.startsWith("update ");
        String payload = input.substring(input.indexOf(' ') + 1).trim();
        Long id = null;
        if (update) {
            int separator = payload.indexOf('|');
            if (separator < 1) {
                throw new IllegalArgumentException("Для update спочатку вкажіть ID");
            }
            id = Long.valueOf(payload.substring(0, separator).trim());
            payload = payload.substring(separator + 1);
        }
        String[] fields = payload.split("\\|", -1);
        if (fields.length < 3 || fields.length > 4) {
            throw new IllegalArgumentException("Очікується Назва|Курс|Посилання|Примітка");
        }
        String specialty = required(fields[0], "Назва спеціальності");
        String course = nullable(fields[1]);
        String inviteLink = required(fields[2], "Telegram-посилання");
        if (!inviteLink.matches("https://t\\.me/(\\+|joinchat/)[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Некоректне Telegram invite-посилання");
        }
        String note = fields.length == 4 ? nullable(fields[3]) : null;
        boolean active = id == null || directory.findById(id).map(StudyGroup::active).orElse(true);
        StudyGroup saved = directory.save(new StudyGroup(
                id, StudyGroupCategory.UNIVERSITY, specialty, course, inviteLink, note, active));
        send(bot, chatId, "✅ Збережено групу #" + saved.id() + ": " + TelegramHtml.escape(saved.specialty()));
    }

    private void list(TelegramLongPollingBot bot, Long chatId) {
        StringBuilder text = new StringBuilder("👥 <b>Керовані посилання на групи</b>\n\n");
        for (StudyGroup group : directory.findAll()) {
            text.append(group.active() ? "✅ " : "⛔ ")
                    .append("#").append(group.id()).append(" ")
                    .append(TelegramHtml.escape(group.specialty()));
            if (group.hasCourse()) {
                text.append(" / ").append(TelegramHtml.escape(group.course()));
            }
            text.append("\n");
        }
        text.append("\n").append(usage());
        send(bot, chatId, text.toString());
    }

    private String required(String value, String field) {
        String result = nullable(value);
        if (result == null) {
            throw new IllegalArgumentException(field + " не може бути порожнім");
        }
        return result;
    }

    private String nullable(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    private String usage() {
        return "<b>Команди:</b>\n"
                + "/groupadmin list\n"
                + "/groupadmin add Назва|1 курс|https://t.me/+...|Примітка\n"
                + "/groupadmin update ID|Назва|1 курс|https://t.me/+...|Примітка\n"
                + "/groupadmin enable ID\n"
                + "/groupadmin disable ID";
    }

    private void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            SendMessage request = new SendMessage();
            request.setChatId(chatId);
            request.setParseMode("HTML");
            request.disableWebPagePreview();
            request.setText(text);
            bot.execute(request);
        } catch (TelegramApiException exception) {
            log.warn("Не вдалося надіслати відповідь /groupadmin у чат {}", chatId, exception);
        }
    }
}
