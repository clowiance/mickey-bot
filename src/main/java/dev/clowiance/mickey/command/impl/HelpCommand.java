package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
<<<<<<< HEAD
<<<<<<< HEAD
import dev.clowiance.mickey.handler.helper.MenuImageSender;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.telegram.menu.MenuMessenger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
<<<<<<< HEAD
<<<<<<< HEAD
import org.telegram.telegrambots.meta.api.objects.Message;
=======
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
import org.telegram.telegrambots.meta.api.objects.Message;
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

@Component
@RequiredArgsConstructor
public class HelpCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;
<<<<<<< HEAD
<<<<<<< HEAD
    private final MenuImageSender menuImageSender;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
    private final MenuMessenger menuMessenger;
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    public boolean isInfoCommand() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
<<<<<<< HEAD
        menuImageSender.sendMenuPhoto(bot, message.getChatId(), TextHelper.HELP_TEXT, keyboardHelper.mainMenu());
=======
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        send(bot, message.getChatId(), TextHelper.HELP_TEXT, keyboardHelper.backToMenu());
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
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
        Long ownerId = message.getFrom() != null ? message.getFrom().getId() : null;
        menuMessenger.sendMenu(bot, message.getChatId(), ownerId, TextHelper.HELP_TEXT, keyboardHelper.mainMenu());
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    }
}
