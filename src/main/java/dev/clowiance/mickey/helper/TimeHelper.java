package dev.clowiance.mickey.helper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TimeHelper {

    public static final long PERMANENT = -1L;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(\\d+)\\s*(min|y|mo|w|d|h|s|m)", Pattern.CASE_INSENSITIVE);

    private static final List<String> PERMANENT_KEYWORDS = List.of(
            "perm", "permanent", "назавжди", "forever", "навсегда", "нав'єки", "навіки"
    );

    private static final long SECONDS_IN_MINUTE = 60L;
    private static final long SECONDS_IN_HOUR = 3600L;
    private static final long SECONDS_IN_DAY = 86400L;
    private static final long SECONDS_IN_WEEK = 604800L;
    private static final long SECONDS_IN_MONTH = 2592000L;
    private static final long SECONDS_IN_YEAR = 31536000L;

    public boolean isPermanentToken(String token) {
        if (token == null) {
            return false;
        }
        String normalized = token.trim().toLowerCase();
        return PERMANENT_KEYWORDS.contains(normalized);
    }

    public boolean looksLikeDuration(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (isPermanentToken(token)) {
            return true;
        }
        return TOKEN_PATTERN.matcher(token).find();
    }

    public Long parseDurationSeconds(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        if (isPermanentToken(input)) {
            return PERMANENT;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        long totalSeconds = 0L;
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            totalSeconds += amount * unitToSeconds(unit);
        }
        if (!matched) {
            return null;
        }
        return totalSeconds;
    }

    private long unitToSeconds(String unit) {
        return switch (unit) {
            case "min" -> SECONDS_IN_MINUTE;
            case "s" -> 1L;
            case "h" -> SECONDS_IN_HOUR;
            case "d" -> SECONDS_IN_DAY;
            case "w" -> SECONDS_IN_WEEK;
            case "mo", "m" -> SECONDS_IN_MONTH;
            case "y" -> SECONDS_IN_YEAR;
            default -> 0L;
        };
    }

    public String formatDuration(long totalSeconds) {
        if (totalSeconds == PERMANENT || totalSeconds < 0) {
            return "назавжди";
        }
        if (totalSeconds == 0) {
            return "0с";
        }
        long remaining = totalSeconds;
        long years = remaining / SECONDS_IN_YEAR;
        remaining %= SECONDS_IN_YEAR;
        long months = remaining / SECONDS_IN_MONTH;
        remaining %= SECONDS_IN_MONTH;
        long weeks = remaining / SECONDS_IN_WEEK;
        remaining %= SECONDS_IN_WEEK;
        long days = remaining / SECONDS_IN_DAY;
        remaining %= SECONDS_IN_DAY;
        long hours = remaining / SECONDS_IN_HOUR;
        remaining %= SECONDS_IN_HOUR;
        long minutes = remaining / SECONDS_IN_MINUTE;
        remaining %= SECONDS_IN_MINUTE;
        long seconds = remaining;

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("р ");
        if (months > 0) sb.append(months).append("міс ");
        if (weeks > 0) sb.append(weeks).append("тижн ");
        if (days > 0) sb.append(days).append("дн ");
        if (hours > 0) sb.append(hours).append("год ");
        if (minutes > 0) sb.append(minutes).append("хв ");
        if (seconds > 0) sb.append(seconds).append("с ");
        return sb.toString().trim();
    }
}
