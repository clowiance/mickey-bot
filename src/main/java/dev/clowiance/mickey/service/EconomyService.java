package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.EconomyAccount;
import dev.clowiance.mickey.repository.EconomyAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class EconomyService {

    private static final long DAILY_MIN = 50L;
    private static final long DAILY_MAX = 200L;
    private static final long DAILY_COOLDOWN_HOURS = 24L;
    private static final long DAILY_STREAK_WINDOW_HOURS = 48L;
    private static final long DAILY_STREAK_BONUS_CAP = 200L;
    private static final long DAILY_STREAK_BONUS_STEP = 10L;
    private static final long MAX_ALLOWED_BALANCE = 1_000_000_000L;

    private final EconomyAccountRepository repository;

    public record DailyResult(boolean success, long reward, int streak, Duration cooldownRemaining) {

        public static DailyResult success(long reward, int streak) {
            return new DailyResult(true, reward, streak, null);
        }

        public static DailyResult cooldown(Duration remaining) {
            return new DailyResult(false, 0L, 0, remaining);
        }
    }

    public synchronized EconomyAccount getOrCreate(Long userId) {
        return repository.findById(userId).orElseGet(() -> {
            LocalDateTime now = LocalDateTime.now();
            EconomyAccount account = EconomyAccount.builder()
                    .userId(userId)
                    .balance(0L)
                    .totalEarned(0L)
                    .dailyStreak(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            return repository.save(account);
        });
    }

    public long getBalance(Long userId) {
        return getOrCreate(userId).getBalance();
    }

    public synchronized EconomyAccount deposit(Long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сума нарахування повинна бути додатною");
        }
        EconomyAccount account = getOrCreate(userId);
        long newBalance = Math.min(MAX_ALLOWED_BALANCE, account.getBalance() + amount);
        long earnedDelta = newBalance - account.getBalance();

        account.setBalance(newBalance);
        account.setTotalEarned(account.getTotalEarned() + earnedDelta);
        account.setUpdatedAt(LocalDateTime.now());
        return repository.save(account);
    }

    public synchronized EconomyAccount withdraw(Long userId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сума списання повинна бути додатною");
        }
        EconomyAccount account = getOrCreate(userId);
        long newBalance = Math.max(0L, account.getBalance() - amount);
        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());
        return repository.save(account);
    }

    public synchronized EconomyAccount setBalance(Long userId, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Баланс не може бути від'ємним");
        }
        long safeAmount = Math.min(amount, MAX_ALLOWED_BALANCE);
        EconomyAccount account = getOrCreate(userId);
        account.setBalance(safeAmount);
        if (safeAmount > account.getTotalEarned()) {
            account.setTotalEarned(safeAmount);
        }
        account.setUpdatedAt(LocalDateTime.now());
        return repository.save(account);
    }

    public synchronized EconomyAccount reset(Long userId) {
        EconomyAccount account = getOrCreate(userId);
        account.setBalance(0L);
        account.setTotalEarned(0L);
        account.setDailyStreak(0);
        account.setLastDailyAt(null);
        account.setUpdatedAt(LocalDateTime.now());
        return repository.save(account);
    }

    public synchronized DailyResult claimDaily(Long userId) {
        EconomyAccount account = getOrCreate(userId);
        LocalDateTime now = LocalDateTime.now();

        if (account.getLastDailyAt() != null && account.getLastDailyAt().plusHours(DAILY_COOLDOWN_HOURS).isAfter(now)) {
            Duration remaining = Duration.between(now, account.getLastDailyAt().plusHours(DAILY_COOLDOWN_HOURS));
            return DailyResult.cooldown(remaining);
        }

        boolean streakContinues = account.getLastDailyAt() != null
                && account.getLastDailyAt().plusHours(DAILY_STREAK_WINDOW_HOURS).isAfter(now);
        int newStreak = streakContinues ? account.getDailyStreak() + 1 : 1;

        long base = ThreadLocalRandom.current().nextLong(DAILY_MIN, DAILY_MAX + 1L);
        long bonus = Math.min(newStreak * DAILY_STREAK_BONUS_STEP, DAILY_STREAK_BONUS_CAP);
        long reward = base + bonus;

        long newBalance = Math.min(MAX_ALLOWED_BALANCE, account.getBalance() + reward);
        long earnedDelta = newBalance - account.getBalance();

        account.setBalance(newBalance);
        account.setTotalEarned(account.getTotalEarned() + earnedDelta);
        account.setDailyStreak(newStreak);
        account.setLastDailyAt(now);
        account.setUpdatedAt(now);
        repository.save(account);

        return DailyResult.success(reward, newStreak);
    }

    public List<EconomyAccount> leaderboard(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return repository.findAllByOrderByBalanceDesc().stream().limit(safeLimit).toList();
    }
}
