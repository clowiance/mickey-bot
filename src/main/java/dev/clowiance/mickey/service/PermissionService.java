package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatInfo;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.repository.ChatInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ChatInfoRepository chatInfoRepository;
    private volatile Long botUserId;

    private static final long BOT_PERMISSION_TTL_NANOS = Duration.ofMinutes(5).toNanos();
    private static final long USER_ADMIN_TTL_NANOS = Duration.ofMinutes(2).toNanos();

    private final ConcurrentMap<Long, PermissionCacheEntry> botPermissionCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChatUserKey, PermissionCacheEntry> userAdminCache = new ConcurrentHashMap<>();

    public void setBotUserId(Long botUserId) {
        this.botUserId = botUserId;
    }

    public boolean validateBotPermissions(TelegramLongPollingBot bot, Long chatId) {
        return validateBotPermissions(bot, chatId, false);
    }

    public boolean refreshBotPermissions(TelegramLongPollingBot bot, Long chatId) {
        return validateBotPermissions(bot, chatId, true);
    }

    private boolean validateBotPermissions(TelegramLongPollingBot bot, Long chatId, boolean forceRefresh) {
        PermissionCacheEntry cached = botPermissionCache.get(chatId);
        if (!forceRefresh && cached != null && cached.isFresh(BOT_PERMISSION_TTL_NANOS)) {
            return cached.allowed();
        }
        if (botUserId == null) {
            try {
                var me = bot.execute(new org.telegram.telegrambots.meta.api.methods.GetMe());
                botUserId = me.getId();
            } catch (TelegramApiException e) {
                log.warn("Не вдалося отримати Telegram ID бота", e);
                return fallbackPermission(chatId, cached);
            }
        }
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(botUserId);
            var member = bot.execute(gcm);
            boolean hasRights = false;
            if (member instanceof ChatMemberOwner) {
                hasRights = true;
            } else if (member instanceof ChatMemberAdministrator administrator) {
                hasRights = hasFullRights(administrator);
            }
            botPermissionCache.put(chatId, PermissionCacheEntry.now(hasRights));
            updateAdminStatus(chatId, hasRights);
            if (!hasRights && shouldWarn(chatId)) {
                sendAdminRequiredNotice(bot, chatId);
                updateWarningTime(chatId);
            }
            return hasRights;
        } catch (TelegramApiException e) {
            log.warn("Не вдалося оновити права бота в чаті {}. Використовую останній відомий стан", chatId, e);
            return fallbackPermission(chatId, cached);
        }
    }

    public boolean isBotAdmin(TelegramLongPollingBot bot, Long chatId) {
        return validateBotPermissions(bot, chatId);
    }

    public boolean isUserChatAdmin(TelegramLongPollingBot bot, Long chatId, Long userId) {
        if (userId == null) {
            return false;
        }
        ChatUserKey key = new ChatUserKey(chatId, userId);
        PermissionCacheEntry cached = userAdminCache.get(key);
        if (cached != null && cached.isFresh(USER_ADMIN_TTL_NANOS)) {
            return cached.allowed();
        }
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(userId);
            var member = bot.execute(gcm);
            boolean isAdmin = member instanceof ChatMemberOwner || member instanceof ChatMemberAdministrator;
            userAdminCache.put(key, PermissionCacheEntry.now(isAdmin));
            return isAdmin;
        } catch (TelegramApiException e) {
            log.warn("Не вдалося перевірити адміністратора {} у чаті {}", userId, chatId, e);
            return cached != null && cached.allowed();
        }
    }

    private boolean fallbackPermission(Long chatId, PermissionCacheEntry cached) {
        if (cached != null) {
            return cached.allowed();
        }
        return chatInfoRepository.findById(chatId)
                .map(ChatInfo::getAdminOk)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

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

    public boolean shouldWarn(Long chatId) {
        ChatInfo info = chatInfoRepository.findById(chatId).orElse(null);
        if (info == null) {
            return true;
        }
        return info.getLastWarningAt() == null || info.getLastWarningAt().isBefore(LocalDateTime.now().minusMinutes(5));
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

    private void sendAdminRequiredNotice(TelegramLongPollingBot bot, Long chatId) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(TextHelper.ADMIN_REQUIRED);
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn("Не вдалося надіслати нагадування про права бота в чат {}", chatId, e);
        }
    }

    private record ChatUserKey(Long chatId, Long userId) {
    }

    private record PermissionCacheEntry(boolean allowed, long checkedAtNanos) {
        private static PermissionCacheEntry now(boolean allowed) {
            return new PermissionCacheEntry(allowed, System.nanoTime());
        }

        private boolean isFresh(long ttlNanos) {
            return System.nanoTime() - checkedAtNanos < ttlNanos;
        }
    }
}
