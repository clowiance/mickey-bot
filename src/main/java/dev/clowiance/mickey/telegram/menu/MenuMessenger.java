package dev.clowiance.mickey.telegram.menu;

import dev.clowiance.mickey.handler.helper.MenuImageSender;
import dev.clowiance.mickey.handler.helper.MenuMediaUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Єдина точка показу розділів меню: тримає прив'язку меню до його власника й обирає
 * спосіб доставки — підпис під фото для коротких розділів, окреме текстове повідомлення
 * для довгих (каталог, /help), бо підпис у Telegram обмежений 1024 символами.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuMessenger {

    private static final int PHOTO_CAPTION_LIMIT = 1024;
    private static final int MESSAGE_LIMIT = 4096;

    private final MenuImageSender menuImageSender;
    private final MenuMediaUpdater menuMediaUpdater;
    private final MenuOwnershipRegistry ownershipRegistry;

    /**
     * Надсилає новий розділ меню й закріплює його за користувачем, який його відкрив.
     */
    public void sendMenu(TelegramLongPollingBot bot, Long chatId, Long ownerId,
                         String text, InlineKeyboardMarkup keyboard) {
        Integer messageId = isLong(text)
                ? sendAsText(bot, chatId, text, keyboard)
                : menuImageSender.sendMenuPhoto(bot, chatId, text, keyboard);
        ownershipRegistry.register(chatId, messageId, ownerId);
    }

    /**
     * Оновлює вже відкрите меню на місці. Якщо новий розділ не вміщається в підпис,
     * старе повідомлення видаляється, а розділ надсилається текстом.
     */
    public void updateMenu(TelegramLongPollingBot bot, MaybeInaccessibleMessage original, Long chatId,
                           Integer messageId, Long ownerId, String text, InlineKeyboardMarkup keyboard) {
        boolean hasPhoto = original instanceof Message msg && msg.hasPhoto();

        if (isLong(text)) {
            deleteMessage(bot, chatId, messageId);
            ownershipRegistry.forget(chatId, messageId);
            ownershipRegistry.register(chatId, sendAsText(bot, chatId, text, keyboard), ownerId);
            return;
        }

        if (hasPhoto) {
            menuMediaUpdater.editToMenuPhoto(bot, chatId, messageId, text, keyboard);
            ownershipRegistry.register(chatId, messageId, ownerId);
            return;
        }

        deleteMessage(bot, chatId, messageId);
        ownershipRegistry.forget(chatId, messageId);
        ownershipRegistry.register(chatId, menuImageSender.sendMenuPhoto(bot, chatId, text, keyboard), ownerId);
    }

    public boolean isOwnedByOther(Long chatId, Integer messageId, Long userId) {
        return ownershipRegistry.isOwnedByOther(chatId, messageId, userId);
    }

    private boolean isLong(String text) {
        return text != null && text.length() > PHOTO_CAPTION_LIMIT;
    }

    /**
     * Ділить текст по межах рядків, щоб не впертися в ліміт повідомлення,
     * і чіпляє клавіатуру до останньої частини.
     */
    private Integer sendAsText(TelegramLongPollingBot bot, Long chatId, String text, InlineKeyboardMarkup keyboard) {
        List<String> chunks = split(text);
        Integer lastMessageId = null;
        for (int i = 0; i < chunks.size(); i++) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.disableWebPagePreview();
            sendMessage.setText(chunks.get(i));
            if (i == chunks.size() - 1 && keyboard != null) {
                sendMessage.setReplyMarkup(keyboard);
            }
            try {
                lastMessageId = bot.execute(sendMessage).getMessageId();
            } catch (TelegramApiException e) {
                log.error("Не вдалося надіслати розділ меню", e);
            }
        }
        return lastMessageId;
    }

    private List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();
        for (String line : text.split("\n", -1)) {
            if (chunk.length() + line.length() + 1 > MESSAGE_LIMIT && chunk.length() > 0) {
                chunks.add(chunk.toString());
                chunk = new StringBuilder();
            }
            if (chunk.length() > 0) {
                chunk.append("\n");
            }
            chunk.append(line);
        }
        chunks.add(chunk.toString());
        return chunks;
    }

    private void deleteMessage(TelegramLongPollingBot bot, Long chatId, Integer messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(messageId);
            bot.execute(deleteMessage);
        } catch (Exception ignored) {
        }
    }
}
