package dev.clowiance.mickey.handler.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class MenuImageSender {

    public void sendMenuPhoto(TelegramLongPollingBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        String safeText = text != null && text.length() > 1024 ? text.substring(0, 1021) + "..." : text;
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setParseMode("HTML");
            sendPhoto.setCaption(safeText);
            if (keyboard != null) {
                sendPhoto.setReplyMarkup(keyboard);
            }

            ClassPathResource imgFile = new ClassPathResource("images/menu.png");
            if (imgFile.exists()) {
                try (InputStream is = imgFile.getInputStream()) {
                    sendPhoto.setPhoto(new InputFile(is, "menu.png"));
                    bot.execute(sendPhoto);
                    return;
                } catch (IOException e) {
                    log.error("Помилка читання images/menu.png", e);
                }
            }

            ClassPathResource fallbackFile = new ClassPathResource("menu.png");
            if (fallbackFile.exists()) {
                try (InputStream is = fallbackFile.getInputStream()) {
                    sendPhoto.setPhoto(new InputFile(is, "menu.png"));
                    bot.execute(sendPhoto);
                    return;
                } catch (IOException e) {
                    log.error("Помилка читання menu.png", e);
                }
            }

            sendHtml(bot, chatId, text, keyboard);
        } catch (TelegramApiException e) {
            log.error("меседж з фото хес нот бін сент", e);
            sendHtml(bot, chatId, text, keyboard);
        }
    }

    public void sendHtml(TelegramLongPollingBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            sendMessage.setParseMode("HTML");
            if (keyboard != null) {
                sendMessage.setReplyMarkup(keyboard);
            }
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("меседж хес нот бін сент", e);
        }
    }
}
