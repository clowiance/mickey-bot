package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatInfo;
import dev.clowiance.mickey.repository.ChatInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    public boolean isBotAdmin(TelegramLongPollingBot bot, Long chatId) {
        if (botUserId == null) {
            return false;
        }
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(botUserId);
            var member = bot.execute(gcm);
            return member instanceof ChatMemberAdministrator || member instanceof ChatMemberOwner;
        } catch (TelegramApiException e) {
            return false;
        }
    }

    public boolean isChatAdmin(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(userId);
            var member = bot.execute(gcm);
            return member instanceof ChatMemberAdministrator || member instanceof ChatMemberOwner;
        } catch (TelegramApiException e) {
            return false;
        }
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

    public boolean shouldWarn(Long chatId) {
        ChatInfo info = chatInfoRepository.findById(chatId).orElse(null);
        if (info == null) {
            return true;
        }
        return info.getLastWarningAt() == null || info.getLastWarningAt().isBefore(LocalDateTime.now().minusMinutes(10));
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
    }
}
