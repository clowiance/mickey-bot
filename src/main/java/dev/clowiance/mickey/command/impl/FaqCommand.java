package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class FaqCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;

    @Override
    public String getCommand() {
        return "/faq";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        send(bot, message.getChatId(), TextHelper.FAQ_MENU_HINT, keyboardHelper.faqMenu());
    }

    private void send(TelegramLongPollingBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            if (keyboard != null) {
                sendMessage.setReplyMarkup(keyboard);
            }
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }
}
