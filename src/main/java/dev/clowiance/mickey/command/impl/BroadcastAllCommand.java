package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.administration.application.GroupBroadcastService;
import dev.clowiance.mickey.command.ICommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class BroadcastAllCommand implements ICommand {

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final GroupBroadcastService groupBroadcastService;

    @Override
    public String getCommand() {
        return "/broadcastall";
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        String[] parts = args == null ? new String[0] : args.trim().split("\\s+", 4);
        if (parts.length < 4) {
            send(bot, message, help());
            return;
        }
        try {
            Long specialtyId = "all".equalsIgnoreCase(parts[0]) ? null : Long.parseLong(parts[0]);
            LocalDateTime sendAt = LocalDateTime.parse(parts[1] + " " + parts[2], INPUT_FORMAT);
            int count = groupBroadcastService.schedule(specialtyId, parts[3], sendAt);
            send(bot, message, "✅ Розсилку заплановано для <b>" + count + "</b> груп на "
                    + sendAt.format(INPUT_FORMAT) + ".");
        } catch (NumberFormatException | DateTimeParseException exception) {
            send(bot, message, "⚠️ Некоректні параметри.\n\n" + help());
        }
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isDevOnly() {
        return true;
    }

    private String help() {
        return "📣 <b>Розсилка по групах</b>\n\n"
                + "Усі групи:\n<code>/broadcastall all 2026-08-01 12:00 Текст</code>\n\n"
                + "Групи спеціальності:\n<code>/broadcastall SPECIALTY_ID 2026-08-01 12:00 Текст</code>";
    }

    private void send(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId());
            response.setParseMode("HTML");
            response.setText(text);
            bot.execute(response);
        } catch (TelegramApiException ignored) {
        }
    }
}
