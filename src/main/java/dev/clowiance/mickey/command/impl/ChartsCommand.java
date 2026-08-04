package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.analytics.domain.ChartDashboard;
import dev.clowiance.mickey.analytics.presentation.AnalyticsTelegramService;
import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.config.BotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChartsCommand implements ICommand {

    private final AnalyticsTelegramService analyticsTelegramService;
    private final BotProperties botProperties;

    @Override
    public String getCommand() {
        return "/charts";
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
        boolean developer = message.getFrom() != null
                && botProperties.getDevIds() != null
                && botProperties.getDevIds().contains(message.getFrom().getId());

        if (args == null || args.isBlank()) {
            analyticsTelegramService.send(bot, message.getChatId(), ChartDashboard.GROUP);
            return;
        }

        // Невідомий аргумент раніше мовчки ставав «зведенням по групі», тож людина
        // отримувала не ту діаграму й не мала жодного натяку чому.
        Optional<ChartDashboard> requested = ChartDashboard.fromSlug(args);
        if (requested.isEmpty()) {
            reply(bot, message, "⚠️ Немає діаграми «" + escape(args.trim()) + "».\n\n"
                    + ChartDashboard.usage(developer));
            return;
        }

        ChartDashboard dashboard = requested.get();
        if (dashboard.developerOnly() && !developer) {
            reply(bot, message, "🔒 Діаграма «" + dashboard.title()
                    + "» доступна лише розробникам бота.\n\n" + ChartDashboard.usage(false));
            return;
        }

        analyticsTelegramService.send(bot, message.getChatId(), dashboard);
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException exception) {
            log.warn("Не вдалося надіслати підказку /charts у чат {}", message.getChatId(), exception);
        }
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
