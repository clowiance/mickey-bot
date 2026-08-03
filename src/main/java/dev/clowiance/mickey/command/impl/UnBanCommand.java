package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.AdminCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class UnBanCommand implements ICommand {

    private final AdminCommandService adminCommandService;

    @Override
    public String getCommand() {
        return "/unban";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    public boolean isGroupOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        adminCommandService.handleUnBan(bot, message, args);
<<<<<<< HEAD
=======
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        adminCommandService.handleUnban(bot, message, args);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    }
}
