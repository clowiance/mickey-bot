package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.AdminCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class MuteCommand implements ICommand {

    private final AdminCommandService adminCommandService;

    @Override
    public String getCommand() {
        return "/mute";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
<<<<<<< HEAD
    public boolean isGroupOnly() {
        return true;
    }

    @Override
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        adminCommandService.handleMute(bot, message, args);
    }
}
