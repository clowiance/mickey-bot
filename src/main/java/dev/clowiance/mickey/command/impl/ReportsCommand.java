package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.config.BotProperties;
<<<<<<< HEAD
import dev.clowiance.mickey.service.ReportCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
=======
import dev.clowiance.mickey.entity.Report;
import dev.clowiance.mickey.service.PermissionService;
import dev.clowiance.mickey.service.ReportService;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

@Component
@RequiredArgsConstructor
public class ReportsCommand implements ICommand {

<<<<<<< HEAD
    private final ReportCommandService reportCommandService;
    private final BotProperties botProperties;
=======
    private final ReportService reportService;
    private final BotProperties botProperties;
    private final PermissionService permissionService;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6

    @Override
    public String getCommand() {
        return "/reports";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
<<<<<<< HEAD
    public boolean isGroupOnly() {
        return false;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        boolean isDev = botProperties.getDevIds() != null
                && botProperties.getDevIds().contains(message.getFrom().getId());
        reportCommandService.handleListCommand(bot, message, isDev);
=======
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(userId);
        boolean isChatAdmin = permissionService.isChatAdmin(bot, chatId, userId);

        if (!isDev && !isChatAdmin) {
            reply(bot, message, TextHelper.NOT_ADMIN);
            return;
        }

        List<Report> reports;
        if (isDev) {
            reports = reportService.getAllPendingReports();
        } else {
            reports = reportService.getPendingReportsByChat(chatId);
        }

        if (reports.isEmpty()) {
            reply(bot, message, TextHelper.REPORTS_EMPTY);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 <b>Репорти</b>\n\n");
        for (Report r : reports.stream().limit(10).toList()) {
            sb.append("• #").append(r.getId()).append(" ");
            if (r.getTargetUsername() != null) sb.append("@").append(r.getTargetUsername());
            else if (r.getTargetId() != null) sb.append("ID:").append(r.getTargetId());
            else sb.append("невідомий");
            sb.append(" - ").append(r.getReason()).append("\n");
        }
        reply(bot, message, sb.toString());
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
