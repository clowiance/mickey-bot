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

    public void handleDevPanel(TelegramLongPollingBot bot, Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextHelper.DEV_PANEL_HEADER).append("\n\n");
        sb.append("💬 Чатів у базі: ").append(chatInfoRepository.count()).append("\n");
        sb.append("👥 Користувачів у базі: ").append(chatMemberRepository.count()).append("\n");
        sb.append("📋 Записів покарань: ").append(punishmentLogRepository.count()).append("\n");
        sb.append("💱 Економічних рахунків: ").append(economyAccountRepository.count()).append("\n");
        sb.append("🧑‍💻 Розробники: ").append(botProperties.getDevIds()).append("\n");
        sb.append("🤖 Юзернейм бота: @").append(botProperties.getUsername()).append("\n\n");
        sb.append("ℹ️ Повне керування економікою: /economy");
        reply(bot, message, sb.toString());
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
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
