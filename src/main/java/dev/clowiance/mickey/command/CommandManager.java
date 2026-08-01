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

    public void process(String command, TelegramLongPollingBot bot, Message message, String args,
                        CommandAuthorization authorization) {
        ICommand cmd = commands.get(command);
        if (cmd == null) {
            return;
        }
        if (!authorization.allows(cmd.getAccessLevel())) {
            sendNotAdmin(bot, message);
            return;
        }
        cmd.execute(bot, message, args);
    }

    private void sendNotAdmin(TelegramLongPollingBot bot, Message message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(TextHelper.NOT_ADMIN);
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }
}
