package dev.clowiance.mickey.handler;

import dev.clowiance.mickey.command.CommandManager;
import dev.clowiance.mickey.config.BotProperties;
import dev.clowiance.mickey.service.ActivityAuditService;
import dev.clowiance.mickey.service.PermissionService;
import dev.clowiance.mickey.service.ChatMemberService;
import dev.clowiance.mickey.service.LogsCallbackService;
import dev.clowiance.mickey.service.MessageService;
import dev.clowiance.mickey.service.ReportCommandService;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.KeywordHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.handler.helper.MenuImageSender;
import dev.clowiance.mickey.telegram.callback.CallbackContext;
import dev.clowiance.mickey.telegram.callback.CallbackDispatcher;
import dev.clowiance.mickey.telegram.callback.CallbackView;
import dev.clowiance.mickey.telegram.menu.MenuMessenger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MickeyHandler extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final ChatMemberService chatMemberService;
    private final MessageService messageService;
    private final CommandManager commandManager;
    private final KeywordHelper keywordHelper;
    private final KeyboardHelper keyboardHelper;
    private final PermissionService permissionService;
    private final ReportCommandService reportCommandService;
    private final LogsCallbackService logsCallbackService;
    private final ActivityAuditService activityAuditService;
    private final MenuImageSender menuImageSender;
    private final MenuMessenger menuMessenger;
    private final CallbackDispatcher callbackDispatcher;

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
                return;
            }
            if (!update.hasMessage()) {
                return;
            }
            Message message = update.getMessage();

            if (message.getNewChatMembers() != null && !message.getNewChatMembers().isEmpty()) {
                handleNewMembers(message);
                return;
            }

            if (message.hasText()) {
                if (message.getChat() != null && message.getChat().isUserChat()) {
                    handlePrivateText(message);
                } else {
                    handleGroupText(message);
                }
            }
        } catch (Exception e) {
            log.error("обробка оновлення успішно провалилась", e);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            try {
                onUpdateReceived(update);
            } catch (Exception e) {
                log.error("Помилка обробки оновлення", e);
            }
        }
    }

    private void handleNewMembers(Message message) {
        Long chatId = message.getChatId();
        permissionService.ensureChatRecorded(chatId);

        boolean botAdded = message.getNewChatMembers().stream().anyMatch(member ->
                Boolean.TRUE.equals(member.getIsBot()) && member.getUserName() != null
                        && member.getUserName().equalsIgnoreCase(getBotUsername()));

        if (botAdded) {
            menuImageSender.sendMenuPhoto(this, chatId, TextHelper.BOT_ADDED_GREETING, null);
            permissionService.validateBotPermissions(this, chatId);
            return;
        }

        if (!permissionService.validateBotPermissions(this, chatId)) {
            return;
        }

        Chat chat = message.getChat();
        boolean isTargetGroup = botProperties.getTargetGroupUsername() != null
                && !botProperties.getTargetGroupUsername().isBlank()
                && chat.getUserName() != null
                && chat.getUserName().equalsIgnoreCase(botProperties.getTargetGroupUsername());
        String welcomeTemplate = isTargetGroup ? TextHelper.WELCOME_ABITURIENT : TextHelper.WELCOME_STUDENT;

        for (User newMember : message.getNewChatMembers()) {
            if (Boolean.TRUE.equals(newMember.getIsBot())) {
                continue;
            }
            chatMemberService.getOrCreate(newMember);
            long memberCount = chatMemberService.countTotalMembers();
            String name = newMember.getUserName() != null ? "@" + newMember.getUserName() : newMember.getFirstName();
            activityAuditService.logActivity(chatId, newMember.getId(), newMember.getUserName(), "MESSAGE", "JOIN",
                    "Приєднався до чату | Учасник #" + memberCount);
            menuImageSender.sendMenuPhoto(this, chatId, String.format(welcomeTemplate, name), keyboardHelper.welcomeMemberNumber(memberCount));
        }
    }

    private void handlePrivateText(Message message) {
        String text = message.getText().trim();
        User from = message.getFrom();
        if (from == null) {
            return;
        }
        chatMemberService.touch(from);

        if (!text.startsWith("/")) {
            return;
        }

        String[] tokens = text.split("\\s+", 2);
        String command = tokens[0].split("@")[0].toLowerCase();
        String args = tokens.length > 1 ? tokens[1] : "";
        boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(from.getId());
        commandManager.processPrivate(command, this, message, args, isDev);
    }

    private void handleGroupText(Message message) {
        Long chatId = message.getChatId();
        permissionService.ensureChatRecorded(chatId);

        if (!permissionService.validateBotPermissions(this, chatId)) {
            return;
        }

        String text = message.getText().trim();
        User from = message.getFrom();
        if (from == null) {
            return;
        }
        chatMemberService.touch(from);

        if (text.startsWith("/")) {
            String[] tokens = text.split("\\s+", 2);
            String command = tokens[0].split("@")[0].toLowerCase();
            String args = tokens.length > 1 ? tokens[1] : "";
            boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(from.getId());
            boolean isChatAdmin = isDev || permissionService.isUserChatAdmin(this, chatId, from.getId());
            commandManager.process(command, this, message, args, isChatAdmin, isDev);
            return;
        }

        boolean moderated = messageService.moderate(this, message);
        if (moderated) {
            return;
        }

        Optional<String> keywordResponse = keywordHelper.findResponse(text);
        keywordResponse.ifPresent(response -> menuImageSender.sendMenuPhoto(this, chatId, response, keyboardHelper.backToMenu()));
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        User user = callbackQuery.getFrom();
        MaybeInaccessibleMessage originalMessage = callbackQuery.getMessage();

        String data = callbackQuery.getData();
        if (data != null && data.startsWith("R:")) {
            answerCallback(callbackQuery.getId(), null);
            reportCommandService.handleCallback(this, callbackQuery, data);
            return;
        }

        if (data != null && (data.equals("logs_close")
                || data.startsWith("logs_confirm:")
                || data.startsWith("logs_filter:")
                || data.startsWith("logs_page:"))) {
            answerCallback(callbackQuery.getId(), null);
            logsCallbackService.handleCallback(this, callbackQuery, data);
            return;
        }

        if (data != null && data.equals("welcome:member_number")) {
            answerCallback(callbackQuery.getId(), null);
            return;
        }

        if (originalMessage == null || data == null) {
            answerCallback(callbackQuery.getId(), null);
            return;
        }

        Long chatId = originalMessage.getChatId();
        Integer messageId = originalMessage.getMessageId();

        MenuSection section = resolveSection(data, chatId, user);
        if (section == null) {
            answerCallback(callbackQuery.getId(), null);
            return;
        }

        // Інлайн-клавіатура спільна для всього чату, тому чуже меню не перемальовуємо:
        // тому, хто натиснув, відкриваємо власну копію розділу. Так кілька учасників
        // можуть одночасно ходити по меню незалежно один від одного.
        if (menuMessenger.isOwnedByOther(chatId, messageId, user.getId())) {
            answerCallback(callbackQuery.getId(), "Відкрив це меню особисто для тебе 👇");
            menuMessenger.sendMenu(this, chatId, user.getId(), section.text(), section.keyboard());
            return;
        }

        answerCallback(callbackQuery.getId(), null);
        menuMessenger.updateMenu(this, originalMessage, chatId, messageId, user.getId(),
                section.text(), section.keyboard());
    }

    private MenuSection resolveSection(String data, Long chatId, User user) {
        boolean developer = botProperties.getDevIds() != null && botProperties.getDevIds().contains(user.getId());
        boolean chatAdmin = developer || permissionService.isUserChatAdmin(this, chatId, user.getId());
        Optional<CallbackView> moduleView = callbackDispatcher.dispatch(
                new CallbackContext(data, chatId, user.getId(), developer, chatAdmin));
        if (moduleView.isPresent()) {
            CallbackView view = moduleView.get();
            return new MenuSection(view.text(),
                    view.keyboard() != null ? view.keyboard() : keyboardHelper.backToMenu());
        }

        return switch (data) {
            case "menu:main" -> new MenuSection(TextHelper.MENU_TITLE, keyboardHelper.mainMenu());
            case "menu:faq" -> new MenuSection(TextHelper.FAQ_MENU_HINT, keyboardHelper.faqMenu());
            case "menu:help" -> new MenuSection(TextHelper.HELP_TEXT, keyboardHelper.backToMenu());
            case "faq:dorms" -> new MenuSection(TextHelper.FAQ_DORMS, keyboardHelper.backToFaq());
            case "faq:admission" -> new MenuSection(TextHelper.FAQ_ADMISSION, keyboardHelper.backToFaq());
            default -> null;
        };
    }

    private void answerCallback(String callbackQueryId, String toast) {
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQueryId);
            if (toast != null) {
                answer.setText(toast);
            }
            execute(answer);
        } catch (TelegramApiException ignored) {
        }
    }

    private record MenuSection(String text, InlineKeyboardMarkup keyboard) {
    }
}
