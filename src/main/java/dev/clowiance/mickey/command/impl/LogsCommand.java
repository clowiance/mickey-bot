package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.KeyboardHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogsCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;

    @Override
    public String getCommand() {
        return "/logs";
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
        Long chatId = message.getChatId();

        String warningText = "⚠️ <b>Увага! Запит системних логів та аудиту безпеки</b>\n\n" +
                "Ви запросили перегляд захищеного аудиту активності: подій повідомлень, модерації, покарань, варнів та дій користувачів у цьому чаті.\n\n" +
                "Ці дані призначені виключно для адміністраторів з метою забезпечення безпеки та порядку.\n\n" +
                "Натисніть кнопку нижче для підтвердження та відкриття панелі управління логами:";

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("✅ Підтвердити та відкрити логи").callbackData("logs_confirm:" + chatId).build()
                ))
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("❌ Скасувати").callbackData("logs_close").build()
                ))
                .build();

        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(warningText);
            sendMessage.setReplyMarkup(markup);
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }
}
