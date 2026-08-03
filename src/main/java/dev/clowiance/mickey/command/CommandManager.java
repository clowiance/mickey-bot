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
<<<<<<< HEAD

=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(List<ICommand> commandList) {
        for (ICommand cmd : commandList) {
            commands.put(cmd.getCommand(), cmd);
        }
    }

<<<<<<< HEAD
    public ICommand find(String command) {
        return commands.get(command);
    }

    public void process(String command, TelegramLongPollingBot bot, Message message, String args,
                        boolean isChatAdmin, boolean isDev) {
=======
    public void process(String command, TelegramLongPollingBot bot, Message message, String args, boolean isAdmin) {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        ICommand cmd = commands.get(command);
        if (cmd == null) {
            return;
        }
<<<<<<< HEAD
        if (cmd.isDevOnly() && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_DEV);
            return;
        }
        if (cmd.isAdminOnly() && !isChatAdmin && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_ADMIN);
=======
        if (cmd.isAdminOnly() && !isAdmin) {
            sendNotAdmin(bot, message);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
            return;
        }
        cmd.execute(bot, message, args);
    }

<<<<<<< HEAD
    public void processPrivate(String command, TelegramLongPollingBot bot, Message message, String args,
                               boolean isDev) {
        ICommand cmd = commands.get(command);
        if (cmd == null) {
            return;
        }
        if (cmd.isGroupOnly()) {
            sendDenied(bot, message, TextHelper.GROUP_ONLY);
            return;
        }
        if (!isDev && !cmd.isInfoCommand()) {
            sendDenied(bot, message, TextHelper.PRIVATE_RESTRICTED);
            return;
        }
        if (cmd.isDevOnly() && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_DEV);
            return;
        }
        if (cmd.isAdminOnly() && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_ADMIN);
            return;
        }
        cmd.execute(bot, message, args);
    }

    private void sendDenied(TelegramLongPollingBot bot, Message message, String text) {
=======
    private void sendNotAdmin(TelegramLongPollingBot bot, Message message) {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
<<<<<<< HEAD
            sendMessage.setText(text);
=======
            sendMessage.setText(TextHelper.NOT_ADMIN);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }
}
