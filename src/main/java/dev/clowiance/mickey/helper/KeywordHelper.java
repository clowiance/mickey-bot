package dev.clowiance.mickey.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Автовідповіді на ключові слова в груповому чаті.
 *
 * <p>Раніше кожне повідомлення зі словом «ціна» чи «гуртожиток» тягнуло повний блок FAQ,
 * тому під час звичайного обговорення бот дослівно повторював один і той самий текст
 * десятки разів. Тепер на тему діє пауза: повторно та сама довідка в тому ж чаті
 * надсилається не раніше ніж через {@link #COOLDOWN}.
 */
@Component
public class KeywordHelper {

    private static final Duration COOLDOWN = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_TOPICS = 2000;

    private record Topic(String id, List<String> keywords, String response) {
    }

    private final List<Topic> topics = List.of(
            new Topic("nmt", List.of("нмт", "мультипредметн"), TextHelper.KEYWORD_NMT),
            new Topic("deadlines", List.of("дедлайн", "термін подач", "коли подавати"), TextHelper.KEYWORD_DEADLINES),
            new Topic("documents", List.of("документ", "які докум", "паспорт"), TextHelper.KEYWORD_DOCUMENTS),
            new Topic("dorms", List.of("гуртожит", "поселенн"), TextHelper.FAQ_DORMS),
            new Topic("prices", List.of("ціна", "вартість", "скільки коштує", "оплат"), TextHelper.FAQ_PRICES),
            new Topic("specialties", List.of("спеціальност", "напрям підготовк"), TextHelper.FAQ_SPECIALTIES));

    private final Map<String, Instant> lastSent = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
                    return size() > MAX_TRACKED_TOPICS;
                }
            });

    private final Clock clock;

    @Autowired
    public KeywordHelper() {
        this(Clock.systemUTC());
    }

    KeywordHelper(Clock clock) {
        this.clock = clock;
    }

    /**
     * @return довідку, якщо в тексті є ключове слово і тему ще не згадували нещодавно
     */
    public Optional<String> findResponse(Long chatId, String text) {
        if (text == null) {
            return Optional.empty();
        }
        String lower = text.toLowerCase();
        for (Topic topic : topics) {
            if (topic.keywords().stream().anyMatch(lower::contains) && claim(chatId, topic.id())) {
                return Optional.of(topic.response());
            }
        }
        return Optional.empty();
    }

    private boolean claim(Long chatId, String topicId) {
        String key = chatId + ":" + topicId;
        Instant now = clock.instant();
        synchronized (lastSent) {
            Instant previous = lastSent.get(key);
            if (previous != null && previous.plus(COOLDOWN).isAfter(now)) {
                return false;
            }
            lastSent.put(key, now);
        }
        return true;
    }
}
