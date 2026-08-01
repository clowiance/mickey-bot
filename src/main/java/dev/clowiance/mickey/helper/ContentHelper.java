package dev.clowiance.mickey.helper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ContentHelper {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://\\S+)|(www\\.\\S+)|(t\\.me/\\S+)|(vk\\.com/\\S+)|(instagram\\.com/\\S+)|(tiktok\\.com/@\\S+)|" +
                    "(youtube\\.com/\\S+)|(youtu\\.be/\\S+)|(discord\\.gg/\\S+)|(twitch\\.tv/\\S+)|" +
                    "(\\b\\S+\\.(com|net|org|ua|ru|io|xyz|click|top|shop|info|biz|me|gg|tv|su|pro|cc|to|tk|ml|ga|cf|gq)\\b\\S*)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern INVISIBLE_CHARS_PATTERN = Pattern.compile(
            "[\\p{C}\\p{Cf}\\p{Zl}\\p{Zp}\\u200B-\\u200D\\uFEFF\\u00AD]"
    );

    private static final Pattern HOMOGLYPH_NUMBERS = Pattern.compile(
            "[0оОо0οΟоOоОｏ０]"
    );

    private Set<String> profanityWords = new HashSet<>();

    private static final Pattern REPEATED_CHARS = Pattern.compile("(.)\\1{4,}");
    private static final Pattern EMOJI_SPAM = Pattern.compile(
            "([\\x{1F300}-\\x{1FAFF}\\x{2600}-\\x{27BF}]){5,}");

    private final FileHelper fileHelper;

    @PostConstruct
    public void init() {
        reloadProfanityWords();
    }

    public void reloadProfanityWords() {
        Set<String> loaded = fileHelper.readWordsFromFile("filters/profanity.txt");
        if (loaded != null && !loaded.isEmpty()) {
            profanityWords = loaded;
        } else {
            profanityWords = new HashSet<>();
        }
    }

    public boolean containsLink(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String cleanedText = sanitizeText(text);
        if (URL_PATTERN.matcher(cleanedText).find()) {
            return true;
        }
        String spacedUnspaced = cleanedText.replaceAll("\\s+", "");
        return URL_PATTERN.matcher(spacedUnspaced).find();
    }

    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank() || profanityWords.isEmpty()) {
            return false;
        }
        String normalized = deepNormalize(text);
        String compact = normalized.replaceAll("\\s+", "");

        for (String word : profanityWords) {
            String cleanWord = deepNormalize(word);
            if (cleanWord.isBlank()) {
                continue;
            }
            if (normalized.contains(cleanWord) || compact.contains(cleanWord.replaceAll("\\s+", ""))) {
                return true;
            }
        }
        return false;
    }

    public boolean isSpam(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String sanitized = sanitizeText(text);
        if (REPEATED_CHARS.matcher(sanitized).find()) {
            return true;
        }
        if (EMOJI_SPAM.matcher(sanitized).find()) {
            return true;
        }

        long upperCount = sanitized.chars().filter(Character::isUpperCase).count();
        long letterCount = sanitized.chars().filter(Character::isLetter).count();
        if (letterCount > 10 && upperCount > letterCount * 0.70) {
            return true;
        }

        String[] words = sanitized.split("\\s+");
        if (words.length >= 4) {
            long uniqueWordsCount = java.util.Arrays.stream(words).map(String::toLowerCase).distinct().count();
            if ((double) uniqueWordsCount / words.length < 0.35) {
                return true;
            }
        }

        return false;
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        String noInvisible = INVISIBLE_CHARS_PATTERN.matcher(text).replaceAll("");
        return Normalizer.normalize(noInvisible, Normalizer.Form.NFKC);
    }

    private String deepNormalize(String text) {
        String sanitized = sanitizeText(text).toLowerCase();
        String dehomoglyphed = sanitized
                .replace('а', 'a').replace('е', 'e').replace('о', 'o')
                .replace('р', 'p').replace('с', 'c').replace('у', 'y')
                .replace('х', 'x').replace('і', 'i').replace('ї', 'i')
                .replace('є', 'e').replace('ѕ', 's').replace('і', 'i')
                .replace('3', 'e').replace('@', 'a').replace('$', 's')
                .replace('1', 'i').replace('!', 'i').replace('0', 'o');

        return dehomoglyphed.replaceAll("[^a-zа-яєіїґ0-9 ]", " ");
    }
}
