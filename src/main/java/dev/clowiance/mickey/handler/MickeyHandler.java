package dev.clowiance.mickey.handler;

import dev.clowiance.mickey.command.CommandManager;
import dev.clowiance.mickey.config.BotProperties;
import dev.clowiance.mickey.entity.ChatInfo;
import dev.clowiance.mickey.service.PermissionService;
import dev.clowiance.mickey.service.ChatMemberService;
import dev.clowiance.mickey.service.MessageModerationService;
import dev.clowiance.mickey.service.ReportService;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.KeywordHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MickeyHandler extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final ChatMemberService chatMemberService;
    private final MessageModerationService messageModerationService;
    private final CommandManager commandManager;
    private final KeywordHelper keywordHelper;
    private final KeyboardHelper keyboardHelper;
    private final PermissionService permissionService;
    private final ReportService reportService;

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
                handleText(message);
            }
        } catch (Exception e) {
            log.error("обробка оновлення успішно провалилась", e);
        }
    }

    private void handleNewMembers(Message message) {
        Long chatId = message.getChatId();
        permissionService.ensureChatRecorded(chatId);
        boolean botAdded = false;
        for (User newMember : message.getNewChatMembers()) {
            if (Boolean.TRUE.equals(newMember.getIsBot()) && newMember.getUserName() != null
                    && newMember.getUserName().equalsIgnoreCase(getBotUsername())) {
                botAdded = true;
            } else if (!Boolean.TRUE.equals(newMember.getIsBot())) {
                chatMemberService.getOrCreate(newMember);
                String name = newMember.getUserName() != null ? "@" + newMember.getUserName() : newMember.getFirstName();
                String welcomeText = isTargetGroup(chatId)
                        ? TextHelper.WELCOME_ABITURIENT
                        : TextHelper.WELCOME_STUDENT;
                sendHtml(chatId, String.format(welcomeText, name), null);
            }
        }
        if (botAdded) {
            permissionService.recordBotJoin(chatId, message.getDate());
            boolean isAdmin = permissionService.isBotAdmin(this, chatId);
            permissionService.updateAdminStatus(chatId, isAdmin);
            if (!isAdmin) {
                sendHtml(chatId, TextHelper.ADMIN_REQUIRED, null);
            }
        }
    }

    private void handleText(Message message) {
        Long chatId = message.getChatId();
        permissionService.ensureChatRecorded(chatId);
        boolean isAdmin = permissionService.isBotAdmin(this, chatId);
        if (!isAdmin) {
            if (permissionService.shouldWarn(chatId)) {
                sendHtml(chatId, TextHelper.ADMIN_REQUIRED, null);
                permissionService.updateWarningTime(chatId);
            }
            return;
        }

        ChatInfo chatInfo = permissionService.getChatInfo(chatId);
        boolean messageIsNew = chatInfo == null || chatInfo.getBotJoinedAt() == null
                || message.getDate() >= chatInfo.getBotJoinedAt();

        String text = message.getText().trim();
        User from = message.getFrom();
        chatMemberService.touch(from);

        if (text.startsWith("/")) {
            String[] tokens = text.split("\\s+", 2);
            String command = tokens[0].split("@")[0].toLowerCase();
            String args = tokens.length > 1 ? tokens[1] : "";
            boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(from.getId());
            commandManager.process(command, this, message, args, isDev);
            return;
        }

        if (messageIsNew) {
            boolean moderated = messageModerationService.moderate(this, message);
            if (moderated) {
                return;
            }
        }

        Optional<String> keywordResponse = keywordHelper.findResponse(text);
        keywordResponse.ifPresent(response -> sendHtml(chatId, response, keyboardHelper.backToMenu()));
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Long userId = callbackQuery.getFrom().getId();
        boolean isDev = botProperties.getDevIds() != null && botProperties.getDevIds().contains(userId);
        boolean isChatAdmin = permissionService.isChatAdmin(this, chatId, userId);

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
            case "dev:panel" -> {
                if (!isDev) {
                    text = TextHelper.NOT_ADMIN;
                    keyboard = null;
                } else {
                    text = TextHelper.DEV_PANEL_TITLE;
                    keyboard = keyboardHelper.devPanel();
                }
            }
            case "admin:panel" -> {
                if (!isDev && !isChatAdmin) {
                    text = TextHelper.NOT_ADMIN;
                    keyboard = null;
                } else {
                    text = TextHelper.ADMIN_PANEL_TITLE;
                    keyboard = keyboardHelper.adminPanel();
                }
            }
            case "dev:reports" -> {
                if (!isDev) {
                    text = TextHelper.NOT_ADMIN;
                } else {
                    text = reportService.formatAllPendingReports();
                }
                keyboard = null;
            }
            case "admin:reports" -> {
                if (!isDev && !isChatAdmin) {
                    text = TextHelper.NOT_ADMIN;
                } else {
                    text = reportService.formatChatPendingReports(chatId);
                }
                keyboard = null;
            }
            case "dev:chats" -> {
                if (!isDev) {
                    text = TextHelper.NOT_ADMIN;
                } else {
                    text = permissionService.formatChatsList();
                }
                keyboard = null;
            }
            case "dev:stats", "admin:stats" -> {
                text = "📊 Статистика буде тут.";
                keyboard = null;
            }
            default -> text = TextHelper.MENU_TITLE;
        }

        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setParseMode("HTML");
            if (keyboard != null) {
                edit.setReplyMarkup(keyboard);
            }
            execute(edit);

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("колбек викаблучується", e);
        }
    }

    private boolean isTargetGroup(Long chatId) {
        String target = botProperties.getTargetGroupUsername();
        if (target == null || target.isBlank()) {
            return false;
        }
        try {
            GetChat getChat = new GetChat();
            getChat.setChatId(chatId);
            Chat chatResult = execute(getChat);
            return chatResult.getUserName() != null && chatResult.getUserName().equalsIgnoreCase(target);
        } catch (TelegramApiException e) {
            return false;
        }
    }

    private void sendHtml(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            sendMessage.setParseMode("HTML");
            if (keyboard != null) {
                sendMessage.setReplyMarkup(keyboard);
            }
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("меседж хес нот бін сент", e);
        }
    }
}
