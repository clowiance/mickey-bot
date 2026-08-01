package dev.clowiance.mickey.helper;

import dev.clowiance.mickey.entity.ChatMember;
import dev.clowiance.mickey.service.ChatMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetHelper {

    private final ChatMemberService chatMemberService;

    public record ResolvedTarget(Long id, String username, String firstName, String remainingArgs, boolean found) {

        public String mention() {
            if (username != null && !username.isBlank()) {
                return "@" + username;
            }
            if (firstName != null && !firstName.isBlank()) {
                return firstName;
            }
            return id != null ? String.valueOf(id) : "невідомий";
        }
    }

    public ResolvedTarget resolve(TelegramLongPollingBot bot, Message message, String argsText) {
        String safeArgs = argsText == null ? "" : argsText.trim();

        if (safeArgs.isEmpty()) {
            ResolvedTarget replyTarget = fromReply(message, "");
            if (replyTarget != null) {
                return replyTarget;
            }
            return new ResolvedTarget(null, null, null, safeArgs, false);
        }

        String[] tokens = safeArgs.split("\\s+", 2);
        String candidate = tokens[0];
        String remaining = tokens.length > 1 ? tokens[1] : "";

        List<MessageEntity> entities = message.getEntities();
        if (entities != null) {
            for (MessageEntity entity : entities) {
                if ("text_mention".equals(entity.getType()) && entity.getUser() != null
                        && candidate.equals(entity.getText())) {
                    User user = entity.getUser();
                    chatMemberService.touch(user);
                    return new ResolvedTarget(user.getId(), user.getUserName(), user.getFirstName(), remaining, true);
                }
            }
        }

        if (candidate.startsWith("@")) {
            String username = candidate.substring(1);
            ChatMember member = chatMemberService.findByUsername(username);
            if (member == null) {
                member = backfillFromAdmins(bot, message, username);
            }
            if (member != null) {
                return new ResolvedTarget(member.getUserId(), member.getUsername(), member.getFirstName(), remaining, true);
            }
            return new ResolvedTarget(null, username, null, remaining, false);
        }

        if (candidate.matches("\\d+")) {
            Long id = Long.parseLong(candidate);
            ChatMember member = chatMemberService.findById(id);
            if (member != null) {
                return new ResolvedTarget(member.getUserId(), member.getUsername(), member.getFirstName(), remaining, true);
            }
            return new ResolvedTarget(id, null, null, remaining, true);
        }

        ResolvedTarget replyTarget = fromReply(message, safeArgs);
        if (replyTarget != null) {
            return replyTarget;
        }

        return new ResolvedTarget(null, null, null, safeArgs, false);
    }

    private ResolvedTarget fromReply(Message message, String remaining) {
        Message replyTo = message.getReplyToMessage();
        if (replyTo == null || replyTo.getFrom() == null) {
            return null;
        }
        User user = replyTo.getFrom();
        if (Boolean.TRUE.equals(user.getIsBot())) {
            return null;
        }
        chatMemberService.touch(user);
        return new ResolvedTarget(user.getId(), user.getUserName(), user.getFirstName(), remaining, true);
    }

    private ChatMember backfillFromAdmins(TelegramLongPollingBot bot, Message message, String username) {
        if (bot == null || message == null) {
            return null;
        }
        Chat chat = message.getChat();
        if (chat == null || chat.isUserChat()) {
            return null;
        }
        Long chatId = message.getChatId();
        if (chatId == null) {
            return null;
        }
        try {
            GetChatAdministrators getAdmins = new GetChatAdministrators();
            getAdmins.setChatId(chatId);
            List<org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember> admins = bot.execute(getAdmins);
            if (admins != null) {
                for (org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember admin : admins) {
                    User user = admin.getUser();
                    if (user != null && username.equalsIgnoreCase(user.getUserName())) {
                        return chatMemberService.touch(user);
                    }
                }
            }
        } catch (TelegramApiException e) {
            log.debug("не вдалося отримати список адміністраторів чату {} для пошуку @{}", chatId, username, e);
        }
        return null;
    }
}
