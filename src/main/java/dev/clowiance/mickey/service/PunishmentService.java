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

    public void log(Long userId, String username, String type, String reason, Long adminId) {
        PunishmentLog log = PunishmentLog.builder()
                .userId(userId)
                .username(username)
                .type(type)
                .reason(reason)
                .adminId(adminId)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(log);
    }

    public List<PunishmentLog> history(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
