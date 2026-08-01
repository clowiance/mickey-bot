package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.Report;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportNotificationService {

    public void notifyAdmins(TelegramLongPollingBot bot, Report report) {
        try {
            GetChatAdministrators getAdmins = new GetChatAdministrators();
            getAdmins.setChatId(report.getChatId());
            List<ChatMember> admins = bot.execute(getAdmins);

            String text = String.format(TextHelper.REPORT_ADMIN_NOTIFICATION, report.getId());

            for (ChatMember admin : admins) {
                if (admin.getUser().getIsBot()) {
                    continue;
                }
                try {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(admin.getUser().getId());
                    sendMessage.setParseMode("HTML");
                    sendMessage.setText(text);
                    bot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.debug("Не вдалося надіслати сповіщення адміну {} в ЛС: {}", admin.getUser().getId(), e.getMessage());
                }
            }
        } catch (TelegramApiException e) {
            log.error("Не вдалося отримати список адміністраторів чату {} для сповіщення", report.getChatId(), e);
        }
    }
}
