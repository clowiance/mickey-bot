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
<<<<<<< HEAD

=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======

>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager(List<ICommand> commandList) {
        for (ICommand cmd : commandList) {
            commands.put(cmd.getCommand(), cmd);
        }
    }

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    public ICommand find(String command) {
        return commands.get(command);
    }

    public void process(String command, TelegramLongPollingBot bot, Message message, String args,
                        boolean isChatAdmin, boolean isDev) {
<<<<<<< HEAD
=======
    public void process(String command, TelegramLongPollingBot bot, Message message, String args, boolean isAdmin) {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        ICommand cmd = commands.get(command);
        if (cmd == null) {
            return;
        }
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        if (cmd.isDevOnly() && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_DEV);
            return;
        }
        if (cmd.isAdminOnly() && !isChatAdmin && !isDev) {
            sendDenied(bot, message, TextHelper.NOT_ADMIN);
<<<<<<< HEAD
=======
        if (cmd.isAdminOnly() && !isAdmin) {
            sendNotAdmin(bot, message);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
            return;
        }
        cmd.execute(bot, message, args);
    }

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
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
<<<<<<< HEAD
=======
    private void sendNotAdmin(TelegramLongPollingBot bot, Message message) {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
<<<<<<< HEAD
<<<<<<< HEAD
            sendMessage.setText(text);
=======
            sendMessage.setText(TextHelper.NOT_ADMIN);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
            sendMessage.setText(text);
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }
}
