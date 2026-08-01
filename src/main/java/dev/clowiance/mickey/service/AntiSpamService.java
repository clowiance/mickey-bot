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

    private final Map<Long, Deque<HistoryEntry>> messageHistory = new ConcurrentHashMap<>();
    private final Map<Long, Deque<Long>> sendTimestamps = new ConcurrentHashMap<>();

    private record HistoryEntry(String normalizedText, long timestamp) {
    }

    public synchronized boolean isDuplicateOrSimilarSpam(Long userId, String rawText) {
        if (userId == null || rawText == null || rawText.isBlank()) {
            return false;
        }
        String normalized = normalize(rawText);
        if (normalized.length() < 3) {
            return false;
        }

        long now = System.currentTimeMillis();
        Deque<HistoryEntry> history = messageHistory.computeIfAbsent(userId, k -> new ArrayDeque<>());
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

    public synchronized boolean isFastSending(Long userId) {
        if (userId == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = sendTimestamps.computeIfAbsent(userId, k -> new ArrayDeque<>());
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
        return text.toLowerCase().replaceAll("[^а-щьюяіїєґa-z0-9]", "");
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
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}
