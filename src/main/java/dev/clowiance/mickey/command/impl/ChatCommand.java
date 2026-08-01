package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPermissions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class ChatCommand implements ICommand {

    @Override
    public String getCommand() {
        return "/chat";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isGroupOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        if (args == null || args.isBlank()) {
            sendReply(bot, message, "ℹ️ Формат: <code>/chat on/off</code>");
            return;
        }

        String mode = args.trim().toLowerCase();
        Long chatId = message.getChatId();

        ChatPermissions chatPermissions = new ChatPermissions();

        if (mode.equals("off")) {
            chatPermissions.setCanSendMessages(false);
            chatPermissions.setCanSendOtherMessages(false);
            chatPermissions.setCanAddWebPagePreviews(false);
            chatPermissions.setCanChangeInfo(false);
            chatPermissions.setCanInviteUsers(false);
            chatPermissions.setCanPinMessages(false);

            SetChatPermissions permissions = new SetChatPermissions();
            permissions.setChatId(chatId);
            permissions.setPermissions(chatPermissions);

            try {
                bot.execute(permissions);
                sendReply(bot, message, "🔇 <b>Режим тиші увімкнено!</b> Тепер писати в чат можуть лише адміністратори.");
            } catch (TelegramApiException e) {
                sendReply(bot, message, "❌ Не вдалося змінити права чату. Перевірте, чи є у бота права адміністратора.");
            }
        } else if (mode.equals("on")) {
            chatPermissions.setCanSendMessages(true);
            chatPermissions.setCanSendOtherMessages(true);
            chatPermissions.setCanAddWebPagePreviews(true);
            chatPermissions.setCanChangeInfo(false);
            chatPermissions.setCanInviteUsers(true);
            chatPermissions.setCanPinMessages(false);

            SetChatPermissions permissions = new SetChatPermissions();
            permissions.setChatId(chatId);
            permissions.setPermissions(chatPermissions);

            try {
                bot.execute(permissions);
                sendReply(bot, message, "🔊 <b>Режим тиші вимкнено!</b> Учасники знову можуть писати в чат.");
            } catch (TelegramApiException e) {
                sendReply(bot, message, "❌ Не вдалося змінити права чату. Перевірте, чи є у бота права адміністратора.");
            }
        } else {
            sendReply(bot, message, "ℹ️ Невідомий параметр. Використовуйте: <code>/chat on</code> або <code>/chat off</code>");
        }
    }

    private void sendReply(TelegramLongPollingBot bot, Message message, String text) {
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
