package dev.clowiance.mickey.handler.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class MenuMediaUpdater {

    public void editToMenuPhoto(TelegramLongPollingBot bot, Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        String safeText = text != null && text.length() > 1024 ? text.substring(0, 1021) + "..." : text;
        try {
            EditMessageCaption editCaption = new EditMessageCaption();
            editCaption.setChatId(chatId);
            editCaption.setMessageId(messageId);
            editCaption.setCaption(safeText);
            editCaption.setParseMode("HTML");
            if (keyboard != null) {
                editCaption.setReplyMarkup(keyboard);
            }
            bot.execute(editCaption);
        } catch (TelegramApiException ex) {
            String message = ex.getMessage();
            if (message != null && (message.contains("message is not modified") || message.contains("Too Many Requests"))) {
                return;
            }
            log.error("Не вдалося оновити caption меню", ex);
        }
    }
}
