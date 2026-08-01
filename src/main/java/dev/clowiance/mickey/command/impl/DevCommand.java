package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.DevCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class DevCommand implements ICommand {

    private final DevCommandService devCommandService;

    @Override
    public String getCommand() {
        return "/dev";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isDevOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        devCommandService.handleDevPanel(bot, message);
    }
}
