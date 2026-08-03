package dev.clowiance.mickey.command;

import dev.clowiance.mickey.helper.TextHelper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandManager {

    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(List<ICommand> commandList) {
        for (ICommand cmd : commandList) {
            commands.put(cmd.getCommand(), cmd);
        }
    }

    public ICommand find(String command) {
        return commands.get(command);
    }

    public void process(String command, TelegramLongPollingBot bot, Message message, String args,
                        boolean isChatAdmin, boolean isDev) {
        ICommand cmd = commands.get(command);
        if (cmd == null) {
            return;
        }
        CommandAccess access = cmd.access();
        if (access == CommandAccess.DEVELOPER && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_DEV);
            return;
        }
        if (access == CommandAccess.GROUP_ADMIN && !isChatAdmin && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_ADMIN);
            return;
        }
        cmd.execute(bot, message, args);
    }

    public void processPrivate(String command, TelegramLongPollingBot bot, Message message, String args,
                               boolean isDev) {
        ICommand cmd = commands.get(command);
        if (cmd == null) {
            return;
        }
        CommandAccess access = cmd.access();
        if (cmd.isGroupOnly()) {
            sendDenied(bot, message, TextHelper.GROUP_ONLY);
            return;
        }
        if (access != CommandAccess.PUBLIC_INFO && !isDev) {
            sendDenied(bot, message, TextHelper.PRIVATE_RESTRICTED);
            return;
        }
        cmd.execute(bot, message, args);
    }

    private void sendDenied(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException exception) {
            dev.clowiance.mickey.telegram.TelegramFailures.log("access-denied", message.getChatId(), exception);
        }
    }
}
