package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatInfo;
<<<<<<< HEAD
import dev.clowiance.mickey.helper.TextHelper;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
import dev.clowiance.mickey.repository.ChatInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
<<<<<<< HEAD
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

<<<<<<< HEAD
import java.lang.reflect.Method;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ChatInfoRepository chatInfoRepository;
    private Long botUserId;

    public void setBotUserId(Long botUserId) {
        this.botUserId = botUserId;
    }

<<<<<<< HEAD
    public boolean validateBotPermissions(TelegramLongPollingBot bot, Long chatId) {
        if (botUserId == null) {
            try {
                var me = bot.execute(new org.telegram.telegrambots.meta.api.methods.GetMe());
                botUserId = me.getId();
            } catch (TelegramApiException e) {
                return false;
            }
=======
    public boolean isBotAdmin(TelegramLongPollingBot bot, Long chatId) {
        if (botUserId == null) {
            return false;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        }
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(botUserId);
            var member = bot.execute(gcm);
<<<<<<< HEAD
            boolean hasRights = false;
            if (member instanceof ChatMemberOwner) {
                hasRights = true;
            } else if (member instanceof ChatMemberAdministrator administrator) {
                hasRights = hasFullRights(administrator);
            }
            updateAdminStatus(chatId, hasRights);
            if (!hasRights && shouldWarn(chatId)) {
                sendAdminRequiredNotice(bot, chatId);
                updateWarningTime(chatId);
            }
            return hasRights;
        } catch (TelegramApiException e) {
            updateAdminStatus(chatId, false);
            if (shouldWarn(chatId)) {
                sendAdminRequiredNotice(bot, chatId);
                updateWarningTime(chatId);
            }
=======
            return member instanceof ChatMemberAdministrator || member instanceof ChatMemberOwner;
        } catch (TelegramApiException e) {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
            return false;
        }
    }

<<<<<<< HEAD
    public boolean isBotAdmin(TelegramLongPollingBot bot, Long chatId) {
        return validateBotPermissions(bot, chatId);
    }

    public boolean isUserChatAdmin(TelegramLongPollingBot bot, Long chatId, Long userId) {
        if (userId == null) {
            return false;
        }
=======
    public boolean isChatAdmin(TelegramLongPollingBot bot, Long chatId, Long userId) {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(userId);
            var member = bot.execute(gcm);
<<<<<<< HEAD
            return member instanceof ChatMemberOwner || member instanceof ChatMemberAdministrator;
=======
            return member instanceof ChatMemberAdministrator || member instanceof ChatMemberOwner;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        } catch (TelegramApiException e) {
            return false;
        }
    }

<<<<<<< HEAD
    private static final List<String> REQUIRED_RIGHTS = List.of(
            "getCanDeleteMessages",
            "getCanRestrictMembers",
            "getCanInviteUsers",
            "getCanPinMessages",
            "getCanChangeInfo",
            "getCanManageVideoChats",
            "getCanPromoteMembers",
            "getCanManageChat",
            "getCanPostStories",
            "getCanEditStories",
            "getCanDeleteStories"
    );

    private boolean hasFullRights(ChatMemberAdministrator administrator) {
        for (String getterName : REQUIRED_RIGHTS) {
            try {
                Method method = ChatMemberAdministrator.class.getMethod(getterName);
                Object value = method.invoke(administrator);
                if (!Boolean.TRUE.equals(value)) {
                    log.debug("боту бракує прав {}", getterName);
                    return false;
                }
            } catch (NoSuchMethodException e) {
                log.debug("право {} відсутнє у поточній версії бібліотеки, скіпаю перевірку", getterName);
            } catch (ReflectiveOperationException e) {
                log.warn("не вдалося перевірити право {} у адміністратора", getterName, e);
                return false;
            }
        }
        return true;
    }

=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    public void ensureChatRecorded(Long chatId) {
        if (!chatInfoRepository.existsById(chatId)) {
            ChatInfo info = ChatInfo.builder()
                    .chatId(chatId)
                    .adminOk(false)
                    .lastWarningAt(LocalDateTime.MIN)
                    .build();
            chatInfoRepository.save(info);
        }
    }

<<<<<<< HEAD
=======
    public ChatInfo getChatInfo(Long chatId) {
        return chatInfoRepository.findById(chatId).orElse(null);
    }

    public void recordBotJoin(Long chatId, int date) {
        ChatInfo info = chatInfoRepository.findById(chatId).orElseGet(() ->
                ChatInfo.builder().chatId(chatId).build());
        info.setBotJoinedAt((long) date);
        info.setAdminOk(true);
        chatInfoRepository.save(info);
    }

>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    public boolean shouldWarn(Long chatId) {
        ChatInfo info = chatInfoRepository.findById(chatId).orElse(null);
        if (info == null) {
            return true;
        }
<<<<<<< HEAD
        return info.getLastWarningAt() == null || info.getLastWarningAt().isBefore(LocalDateTime.now().minusMinutes(5));
=======
        return info.getLastWarningAt() == null || info.getLastWarningAt().isBefore(LocalDateTime.now().minusMinutes(10));
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    }

    public void updateWarningTime(Long chatId) {
        chatInfoRepository.findById(chatId).ifPresent(info -> {
            info.setLastWarningAt(LocalDateTime.now());
            chatInfoRepository.save(info);
        });
    }

    public void updateAdminStatus(Long chatId, boolean isAdmin) {
        ChatInfo info = chatInfoRepository.findById(chatId).orElseGet(() ->
                ChatInfo.builder().chatId(chatId).build());
        info.setAdminOk(isAdmin);
        chatInfoRepository.save(info);
    }

    public List<ChatInfo> findAllChats() {
        return chatInfoRepository.findAll();
    }

<<<<<<< HEAD
    private void sendAdminRequiredNotice(TelegramLongPollingBot bot, Long chatId) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(TextHelper.ADMIN_REQUIRED);
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
=======
    public String formatChatsList() {
        List<ChatInfo> chats = findAllChats();
        if (chats.isEmpty()) {
            return "💬 Немає записаних чатів.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("💬 <b>Список чатів</b>\n\n");
        for (ChatInfo chat : chats) {
            sb.append("• ").append(chat.getChatId())
                    .append(" | Адмін: ").append(Boolean.TRUE.equals(chat.getAdminOk()) ? "✅" : "❌")
                    .append("\n");
        }
        return sb.toString();
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    }
}
