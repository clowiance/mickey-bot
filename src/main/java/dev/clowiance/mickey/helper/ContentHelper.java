package dev.clowiance.mickey.helper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ContentHelper {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://\\S+)|(www\\.\\S+)|(t\\.me/\\S+)|(\\S+\\.(com|net|org|ua|ru|io|xyz|click|top|shop)\\b\\S*)",
            Pattern.CASE_INSENSITIVE);

    private static final List<String> PROFANITY_WORDS = List.of(
            "бляд", "хуй", "хуе", "пизд", "єбан", "ебан", "сука", "мудак",
            "гандон", "залуп", "піздец", "довбойоб", "уйоб", "хуйн"
    );

    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{6,}");
    private static final Pattern EMOJI_SPAM = Pattern.compile(
            "([\\x{1F300}-\\x{1FAFF}\\x{2600}-\\x{27BF}]){8,}");

    public boolean containsLink(String text) {
        if (text == null) {
            return false;
        }
        return URL_PATTERN.matcher(text).find();
    }

    public boolean containsProfanity(String text) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase().replaceAll("[^а-щьюяіїєa-z0-9 ]", "");
        for (String word : PROFANITY_WORDS) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSpam(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (REPEATED_CHARS.matcher(text).find()) {
            return true;
        }
        if (EMOJI_SPAM.matcher(text).find()) {
            return true;
        }
        long upperCount = text.chars().filter(Character::isUpperCase).count();
        long letterCount = text.chars().filter(Character::isLetter).count();
        if (letterCount > 15 && upperCount > letterCount * 0.8) {
            return true;
        }
        return false;
    }
}
