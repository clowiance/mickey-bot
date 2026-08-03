package dev.clowiance.mickey.service;

import dev.clowiance.mickey.config.BotProperties;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.repository.ChatInfoRepository;
import dev.clowiance.mickey.repository.ChatMemberRepository;
import dev.clowiance.mickey.repository.EconomyAccountRepository;
import dev.clowiance.mickey.repository.PunishmentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class DevCommandService {

    private final ChatInfoRepository chatInfoRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final PunishmentLogRepository punishmentLogRepository;
    private final EconomyAccountRepository economyAccountRepository;
    private final BotProperties botProperties;
    private final dev.clowiance.mickey.helper.KeyboardHelper keyboardHelper;
    private final dev.clowiance.mickey.telegram.menu.MenuMessenger menuMessenger;

    /**
     * Панель розробника відкривається кнопками {@code KeyboardHelper#devPanel()} — після
     * злиття гілок вони лишилися без жодного виклику, і /dev віддавав самий текст.
     */
    public void handleDevPanel(TelegramLongPollingBot bot, Message message) {
        String text = TextHelper.DEV_PANEL_HEADER + "\n\n"
                + "💬 Чатів у базі: " + chatInfoRepository.count() + "\n"
                + "👥 Користувачів у базі: " + chatMemberRepository.count() + "\n"
                + "📋 Записів покарань: " + punishmentLogRepository.count() + "\n"
                + "💱 Економічних рахунків: " + economyAccountRepository.count() + "\n"
                + "🤖 Юзернейм бота: @" + botProperties.getUsername() + "\n\n"
                + "Обери розділ 👇";
        Long ownerId = message.getFrom() != null ? message.getFrom().getId() : null;
        menuMessenger.sendMenu(bot, message.getChatId(), ownerId, text, keyboardHelper.devPanel());
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException exception) {
            dev.clowiance.mickey.telegram.TelegramFailures.log("dev-command-reply", message.getChatId(), exception);
        }
    }
}
