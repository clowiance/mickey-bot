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
import dev.clowiance.mickey.handler.helper.MenuMediaUpdater;
import dev.clowiance.mickey.telegram.callback.CallbackContext;
import dev.clowiance.mickey.telegram.callback.CallbackDispatcher;
import dev.clowiance.mickey.telegram.callback.CallbackView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
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
    private final MenuMediaUpdater menuMediaUpdater;
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

        if (originalMessage instanceof Message msg) {
            if (msg.getChat() != null && !msg.getChat().isUserChat()) {
                Message replyTo = msg.getReplyToMessage();
                Long menuAuthorId = null;
                if (replyTo != null && replyTo.getFrom() != null) {
                    menuAuthorId = replyTo.getFrom().getId();
                } else if (msg.getForwardFrom() != null) {
                    menuAuthorId = msg.getForwardFrom().getId();
                }

                if (menuAuthorId != null) {
                    boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(user.getId());
                    if (!menuAuthorId.equals(user.getId()) && !isDev) {
                        try {
                            AnswerCallbackQuery answer = new AnswerCallbackQuery();
                            answer.setCallbackQueryId(callbackQuery.getId());
                            answer.setText("Цією кнопкою може користуватися лише той, хто викликав меню!");
                            answer.setShowAlert(true);
                            execute(answer);
                        } catch (TelegramApiException ignored) {
                        }
                        return;
                    }
                }
            }
        }

        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            execute(answer);
        } catch (TelegramApiException ignored) {
        }

        String data = callbackQuery.getData();
        if (data != null && data.startsWith("R:")) {
            reportCommandService.handleCallback(this, callbackQuery, data);
            return;
        }

        if (data != null && (data.equals("logs_close")
                || data.startsWith("logs_confirm:")
                || data.startsWith("logs_filter:")
                || data.startsWith("logs_page:"))) {
            logsCallbackService.handleCallback(this, callbackQuery, data);
            return;
        }

        if (data != null && data.equals("welcome:member_number")) {
            return;
        }

        if (originalMessage == null) {
            return;
        }

        Long chatId = originalMessage.getChatId();
        Integer messageId = originalMessage.getMessageId();

        if (data != null) {
            boolean developer = botProperties.getDevIds() != null && botProperties.getDevIds().contains(user.getId());
            boolean chatAdmin = developer || permissionService.isUserChatAdmin(this, chatId, user.getId());
            Optional<CallbackView> moduleView = callbackDispatcher.dispatch(
                    new CallbackContext(data, chatId, user.getId(), developer, chatAdmin));
            if (moduleView.isPresent()) {
                CallbackView view = moduleView.get();
                renderMenuView(originalMessage, chatId, messageId, view.text(),
                        view.keyboard() != null ? view.keyboard() : keyboardHelper.backToMenu());
                return;
            }
        }

        String text;
        var keyboard = keyboardHelper.backToMenu();

        switch (data) {
            case "menu:main" -> {
                text = TextHelper.MENU_TITLE;
                keyboard = keyboardHelper.mainMenu();
            }
            case "menu:faq" -> {
                text = TextHelper.FAQ_MENU_HINT;
                keyboard = keyboardHelper.faqMenu();
            }
            case "menu:help" -> text = TextHelper.HELP_TEXT;
            case "faq:specialties" -> text = TextHelper.FAQ_SPECIALTIES;
            case "faq:prices" -> text = TextHelper.FAQ_PRICES;
            case "faq:dorms" -> text = TextHelper.FAQ_DORMS;
            default -> text = TextHelper.MENU_TITLE;
        }

        renderMenuView(originalMessage, chatId, messageId, text, keyboard);
    }

    private void renderMenuView(MaybeInaccessibleMessage originalMessage, Long chatId, Integer messageId,
                                String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup keyboard) {
        boolean hasPhoto = originalMessage instanceof Message msg && msg.hasPhoto();
        boolean hasText = originalMessage instanceof Message msg && msg.hasText() && msg.getText() != null && !msg.getText().isBlank();

        if (hasPhoto) {
            menuMediaUpdater.editToMenuPhoto(this, chatId, messageId, text, keyboard);
        } else if (hasText) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId);
                deleteMessage.setMessageId(messageId);
                execute(deleteMessage);
            } catch (Exception ignored) {
            }
            menuImageSender.sendMenuPhoto(this, chatId, text, keyboard);
        } else {
            menuImageSender.sendMenuPhoto(this, chatId, text, keyboard);
        }
    }
}
