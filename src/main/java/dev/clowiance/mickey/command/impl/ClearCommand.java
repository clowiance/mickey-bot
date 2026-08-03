package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.service.PermissionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;

@Component
public class ClearCommand implements ICommand {

    private final PermissionService permissionService;
    private final TaskScheduler taskScheduler;

    public ClearCommand(PermissionService permissionService,
                        @Qualifier("botTaskScheduler") TaskScheduler taskScheduler) {
        this.permissionService = permissionService;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public String getCommand() {
        return "/clear";
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
        if (!message.getChat().isGroupChat() && !message.getChat().isSuperGroupChat()) {
            return;
        }

        int count = 10;
        if (args != null && !args.isBlank()) {
            String rawArg = args.trim().split("\\s+")[0];
            if (rawArg.contains(".") || rawArg.contains(",")) {
                send(bot, message.getChatId(), TextHelper.CLEAR_INVALID_AMOUNT);
                return;
            }
            try {
                count = Integer.parseInt(rawArg);
            } catch (NumberFormatException e) {
                send(bot, message.getChatId(), TextHelper.CLEAR_INVALID_AMOUNT);
                return;
            }
        }

        if (count < 1 || count > 100) {
            send(bot, message.getChatId(), TextHelper.CLEAR_INVALID_AMOUNT);
            return;
        }

        long chatId = message.getChatId();
        int targetMessageId = message.getMessageId();

        try {
            bot.execute(new DeleteMessage(String.valueOf(chatId), targetMessageId));
        } catch (TelegramApiException ignored) {
        }

        int deletedCount = 0;
        for (int i = 0; i < count; i++) {
            int msgIdToDel = targetMessageId - 1 - i;
            if (msgIdToDel <= 0) {
                break;
            }
            try {
                boolean success = bot.execute(new DeleteMessage(String.valueOf(chatId), msgIdToDel));
                if (success) {
                    deletedCount++;
                }
            } catch (TelegramApiException ignored) {
            }
        }

        try {
            SendMessage notification = new SendMessage();
            notification.setChatId(chatId);
            notification.setText(String.format(TextHelper.CLEAR_SUCCESS, deletedCount));
            Message sent = bot.execute(notification);

            taskScheduler.schedule(() -> {
                try {
                    bot.execute(new DeleteMessage(String.valueOf(chatId), sent.getMessageId()));
                } catch (TelegramApiException ignored) {
                }
            }, Instant.now().plusSeconds(3));
        } catch (TelegramApiException ignored) {
        }
    }

    private void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }
}
