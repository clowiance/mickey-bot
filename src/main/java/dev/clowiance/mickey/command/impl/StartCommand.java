package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class StartCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isInfoCommand() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        sendPhotoWithKeyboard(bot, message.getChatId(), TextHelper.MENU_TITLE, keyboardHelper.mainMenu());
    }

    private void sendPhotoWithKeyboard(TelegramLongPollingBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setCaption(text);
            sendPhoto.setParseMode("HTML");
            if (keyboard != null) {
                sendPhoto.setReplyMarkup(keyboard);
            }

            ClassPathResource resource = new ClassPathResource("images/menu.png");
            try (InputStream inputStream = resource.getInputStream()) {
                sendPhoto.setPhoto(new InputFile(inputStream, "menu.png"));
                bot.execute(sendPhoto);
                return;
            } catch (Exception ignored) {
                ClassPathResource fallback = new ClassPathResource("menu.png");
                try (InputStream inputStream = fallback.getInputStream()) {
                    sendPhoto.setPhoto(new InputFile(inputStream, "menu.png"));
                    bot.execute(sendPhoto);
                }
            }
        } catch (TelegramApiException | java.io.IOException ignored) {
        }
    }
}
