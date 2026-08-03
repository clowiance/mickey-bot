package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.PunishmentLog;
import dev.clowiance.mickey.repository.PunishmentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PunishmentService {

    private final PunishmentLogRepository repository;

    public void log(Long chatId, Long userId, String username, String type, String reason, Long adminId) {
        PunishmentLog log = PunishmentLog.builder()
                .chatId(chatId)
                .userId(userId)
                .username(username)
                .type(type)
                .reason(reason)
                .adminId(adminId)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(log);
    }

    public List<PunishmentLog> history(Long chatId, Long userId) {
        return repository.findByChatIdAndUserIdOrderByCreatedAtDesc(chatId, userId);
    }
}
