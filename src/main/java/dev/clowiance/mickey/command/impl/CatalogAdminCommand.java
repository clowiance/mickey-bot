package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.StudyForm;
import dev.clowiance.mickey.catalog.presentation.CatalogMessageRenderer;
import dev.clowiance.mickey.command.ICommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class CatalogAdminCommand implements ICommand {

    private final UniversityCatalogService catalogService;
    private final CatalogMessageRenderer renderer;

    @Override
    public String getCommand() {
        return "/catalogadmin";
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        try {
            send(bot, message, executeAction(args));
        } catch (IllegalArgumentException exception) {
            send(bot, message, "⚠️ " + exception.getMessage() + "\n\n" + help());
        }
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isDevOnly() {
        return true;
    }

    private String executeAction(String args) {
        if (args == null || args.isBlank()) {
            return help();
        }
        String[] parts = args.trim().split("\\s+");
        return switch (parts[0].toLowerCase()) {
            case "list" -> renderer.renderAdminList(parts.length > 1
                    ? EducationLevel.fromSlug(parts[1])
                    : EducationLevel.BACHELOR);
            case "price" -> updatePrice(parts);
            case "active" -> toggleActive(parts);
            default -> help();
        };
    }

    private String updatePrice(String[] parts) {
        if (parts.length != 4) {
            throw new IllegalArgumentException("Формат зміни ціни некоректний.");
        }
        Long id = Long.parseLong(parts[1]);
        StudyForm form = switch (parts[2].toLowerCase()) {
            case "full" -> StudyForm.FULL_TIME;
            case "part" -> StudyForm.PART_TIME;
            default -> throw new IllegalArgumentException("Форма має бути full або part.");
        };
        Long amount = "none".equalsIgnoreCase(parts[3]) ? null : Long.parseLong(parts[3]);
        var updated = catalogService.updateTuition(id, form, amount);
        return "✅ Ціну оновлено для <b>" + updated.code() + " " + updated.name() + "</b>.";
    }

    private String toggleActive(String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Вкажіть ID спеціальності.");
        }
        var updated = catalogService.toggleActive(Long.parseLong(parts[1]));
        return (updated.active() ? "✅ Активовано: " : "⛔ Деактивовано: ")
                + "<b>" + updated.code() + " " + updated.name() + "</b>.";
    }

    private String help() {
        return "🛠️ <b>Керування каталогом УКД</b>\n\n"
                + "<code>/catalogadmin list bachelor</code>\n"
                + "<code>/catalogadmin list master</code>\n"
                + "<code>/catalogadmin price ID full 50600</code>\n"
                + "<code>/catalogadmin price ID part none</code>\n"
                + "<code>/catalogadmin active ID</code>";
    }

    private void send(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId());
            response.setParseMode("HTML");
            response.setText(text);
            bot.execute(response);
        } catch (TelegramApiException ignored) {
        }
    }
}
