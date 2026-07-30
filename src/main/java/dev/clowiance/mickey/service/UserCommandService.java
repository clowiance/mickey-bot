package dev.clowiance.mickey.service;

import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final PollService pollService;
    private final BroadcastService broadcastService;

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void handlePoll(TelegramLongPollingBot bot, Message message, String argsText) {
        String[] split = argsText.split("\\?", 2);
        if (split.length < 2) {
            reply(bot, message, "⚠️ Формат: /poll Питання? Варіант1;Варіант2;...");
            return;
        }
        String question = split[0].trim();
        List<String> options = Arrays.stream(split[1].split(";"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (options.size() < 2) {
            reply(bot, message, "⚠️ Потрібно щонайменше 2 варіанти відповіді.");
            return;
        }

        boolean created = pollService.createPoll(bot, message.getChatId(), question, options);
        if (created) {
            reply(bot, message, TextHelper.POLL_CREATED);
        } else {
            reply(bot, message, "❌ Не вдалося створити опитування.");
        }
    }

    public void handleSchedule(TelegramLongPollingBot bot, Message message, String argsText) {
        String[] parts = argsText.trim().split("\\s+", 3);
        if (parts.length < 3) {
            reply(bot, message, TextHelper.BROADCAST_HELP);
            return;
        }
        try {
            LocalDateTime sendAt = LocalDateTime.parse(parts[0] + " " + parts[1], INPUT_FORMAT);
            String text = parts[2];
            broadcastService.schedule(message.getChatId(), text, sendAt);
            reply(bot, message, String.format(TextHelper.BROADCAST_SCHEDULED, sendAt.format(INPUT_FORMAT)));
        } catch (DateTimeParseException e) {
            reply(bot, message, "⚠️ Неправильний формат дати. " + TextHelper.BROADCAST_HELP);
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
}
