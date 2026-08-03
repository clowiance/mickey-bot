package dev.clowiance.mickey.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AntiSpamService {

    private static final int HISTORY_SIZE = 6;
    private static final long HISTORY_TTL_MS = 5 * 60 * 1000L;
    private static final double SIMILARITY_THRESHOLD = 0.80;
    private static final int SIMILAR_HITS_REQUIRED = 2;

    private static final int FAST_SEND_COUNT = 4;
    private static final long FAST_SEND_WINDOW_MS = 3500L;
    private static final long TIMESTAMP_TTL_MS = 60 * 1000L;
    private static final long CLEANUP_INTERVAL_MS = 60 * 1000L;
    private static final int MAX_TRACKED_USERS = 10_000;
    private static final int MAX_NORMALIZED_LENGTH = 512;

    private final Map<ChatUserKey, Deque<HistoryEntry>> messageHistory = new ConcurrentHashMap<>();
    private final Map<ChatUserKey, Deque<Long>> sendTimestamps = new ConcurrentHashMap<>();
    private long lastCleanupAt;

    private record ChatUserKey(Long chatId, Long userId) {
    }

    private record HistoryEntry(String normalizedText, long timestamp) {
    }

    public synchronized boolean isDuplicateOrSimilarSpam(Long chatId, Long userId, String rawText) {
        if (chatId == null || userId == null || rawText == null || rawText.isBlank()) {
            return false;
        }
        String normalized = normalize(rawText);
        if (normalized.length() < 3) {
            return false;
        }

        long now = System.currentTimeMillis();
        cleanupIfDue(now);
        Deque<HistoryEntry> history = messageHistory.computeIfAbsent(new ChatUserKey(chatId, userId),
                key -> new ArrayDeque<>());
        history.removeIf(entry -> now - entry.timestamp() > HISTORY_TTL_MS);

        int similarHits = 0;
        for (HistoryEntry entry : history) {
            if (similarity(entry.normalizedText(), normalized) >= SIMILARITY_THRESHOLD) {
                similarHits++;
            }
        }

        history.addLast(new HistoryEntry(normalized, now));
        while (history.size() > HISTORY_SIZE) {
            history.removeFirst();
        }

        return similarHits >= SIMILAR_HITS_REQUIRED;
    }

    public synchronized boolean isFastSending(Long chatId, Long userId) {
        if (chatId == null || userId == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        cleanupIfDue(now);
        Deque<Long> timestamps = sendTimestamps.computeIfAbsent(new ChatUserKey(chatId, userId),
                key -> new ArrayDeque<>());
        timestamps.removeIf(ts -> now - ts > TIMESTAMP_TTL_MS);
        timestamps.addLast(now);
        while (timestamps.size() > FAST_SEND_COUNT + 3) {
            timestamps.removeFirst();
        }

        if (timestamps.size() < FAST_SEND_COUNT) {
            return false;
        }

        Long[] arr = timestamps.toArray(new Long[0]);
        long windowStart = arr[arr.length - FAST_SEND_COUNT];
        return now - windowStart <= FAST_SEND_WINDOW_MS;
    }

    private String normalize(String text) {
        String normalized = text.toLowerCase().replaceAll("[^а-щьюяіїєґa-z0-9]", "");
        return normalized.length() <= MAX_NORMALIZED_LENGTH
                ? normalized
                : normalized.substring(0, MAX_NORMALIZED_LENGTH);
    }

    private double similarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) {
            return 1.0;
        }
        int distance = levenshtein(a, b);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshtein(String a, String b) {
        if (a.length() < b.length()) {
            String swap = a;
            a = b;
            b = swap;
        }
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    private void cleanupIfDue(long now) {
        if (now - lastCleanupAt < CLEANUP_INTERVAL_MS
                && messageHistory.size() <= MAX_TRACKED_USERS
                && sendTimestamps.size() <= MAX_TRACKED_USERS) {
            return;
        }
        messageHistory.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(item -> now - item.timestamp() > HISTORY_TTL_MS);
            return entry.getValue().isEmpty();
        });
        sendTimestamps.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(timestamp -> now - timestamp > TIMESTAMP_TTL_MS);
            return entry.getValue().isEmpty();
        });
        trimToLimit(messageHistory);
        trimToLimit(sendTimestamps);
        lastCleanupAt = now;
    }

    private <T> void trimToLimit(Map<ChatUserKey, T> map) {
        var iterator = map.keySet().iterator();
        while (map.size() > MAX_TRACKED_USERS && iterator.hasNext()) {
            map.remove(iterator.next());
        }
    }
}
