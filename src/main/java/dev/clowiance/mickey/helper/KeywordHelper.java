package dev.clowiance.mickey.helper;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class KeywordHelper {

    private final Map<List<String>, String> keywordMap = new LinkedHashMap<>();

    public KeywordHelper() {
        keywordMap.put(List.of("нмт", "мультипредметн"), TextHelper.KEYWORD_NMT);
        keywordMap.put(List.of("дедлайн", "термін подач", "коли подавати"), TextHelper.KEYWORD_DEADLINES);
        keywordMap.put(List.of("документ", "які докум", "паспорт"), TextHelper.KEYWORD_DOCUMENTS);
        keywordMap.put(List.of("гуртожит", "поселенн"), TextHelper.FAQ_DORMS);
        keywordMap.put(List.of("ціна", "вартість", "скільки коштує", "оплат"), TextHelper.FAQ_PRICES);
        keywordMap.put(List.of("спеціальност", "напрям підготовк"), TextHelper.FAQ_SPECIALTIES);
    }

    public Optional<String> findResponse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String lower = text.toLowerCase();
        for (Map.Entry<List<String>, String> entry : keywordMap.entrySet()) {
            for (String keyword : entry.getKey()) {
                if (lower.contains(keyword)) {
                    return Optional.of(entry.getValue());
                }
            }
        }
        return Optional.empty();
    }
}
