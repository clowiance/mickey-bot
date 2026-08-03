package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
<<<<<<< HEAD
import dev.clowiance.mickey.service.ReportCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
=======
import dev.clowiance.mickey.service.ReportService;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

@Component
@RequiredArgsConstructor
public class ReportCommand implements ICommand {

<<<<<<< HEAD
    private final ReportCommandService reportCommandService;
=======
    private final ReportService reportService;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

    @Override
    public String getCommand() {
        return "/report";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
<<<<<<< HEAD
    public boolean isGroupOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        reportCommandService.handleCreate(bot, message, args);
=======
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        Long chatId = message.getChatId();
        Long reporterId = message.getFrom().getId();
        String reporterUsername = message.getFrom().getUserName();

        Long targetId = null;
        String targetUsername = null;
        String reason;

        if (message.getReplyToMessage() != null) {
            targetId = message.getReplyToMessage().getFrom().getId();
            targetUsername = message.getReplyToMessage().getFrom().getUserName();
            reason = args.isBlank() ? "не вказано" : args;
        } else {
            if (args.isBlank()) {
                reply(bot, message, TextHelper.REPORT_HELP);
                return;
            }
            String[] parts = args.trim().split("\s+", 2);
            if (parts.length < 2) {
                reply(bot, message, TextHelper.REPORT_HELP);
                return;
            }
            String targetArg = parts[0];
            reason = parts[1];
            if (targetArg.startsWith("@")) {
                targetUsername = targetArg.substring(1);
            } else {
                try {
                    targetId = Long.parseLong(targetArg);
                } catch (NumberFormatException e) {
                    reply(bot, message, "⚠️ Вкажи @username або числовий ID користувача.");
                    return;
                }
            }
        }

        reportService.createReport(chatId, reporterId, reporterUsername, targetId, targetUsername, reason);
        reply(bot, message, TextHelper.REPORT_SENT);
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sm = new SendMessage();
            sm.setChatId(message.getChatId());
            sm.setParseMode("HTML");
            sm.setText(text);
            bot.execute(sm);
        } catch (TelegramApiException ignored) {
        }
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    }
}
