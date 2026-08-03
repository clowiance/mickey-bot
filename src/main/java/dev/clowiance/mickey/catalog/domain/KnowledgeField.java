package dev.clowiance.mickey.catalog.domain;

import java.util.Arrays;

/**
 * Галузь знань за літерою коду спеціальності (класифікація 2025 року): F2 — ІТ,
 * G17/G19 — інженерія тощо. Потрібна лише для групування списку: суцільний перелік
 * із 21 позиції очима не читається.
 */
public enum KnowledgeField {

    IT("F", "💻", "Інформаційні технології"),
    ENGINEERING("G", "🏗", "Інженерія, архітектура та будівництво"),
    BUSINESS("D", "📈", "Бізнес, управління та право"),
    SECURITY("K", "🛡", "Безпека та правоохоронна діяльність"),
    HEALTH("I", "⚕️", "Охорона здоров'я"),
    SOCIAL("C", "🧠", "Соціальні науки та журналістика"),
    CULTURE("B", "🎨", "Культура, мистецтво та філологія"),
    SERVICE("J", "🧳", "Сфера обслуговування та туризм"),
    OTHER("", "🎓", "Інші спеціальності");

    private final String codePrefix;
    private final String emoji;
    private final String displayName;

    KnowledgeField(String codePrefix, String emoji, String displayName) {
        this.codePrefix = codePrefix;
        this.emoji = emoji;
        this.displayName = displayName;
    }

    public static KnowledgeField fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        String letter = code.substring(0, 1).toUpperCase();
        return Arrays.stream(values())
                .filter(field -> !field.codePrefix.isEmpty() && field.codePrefix.equals(letter))
                .findFirst()
                .orElse(OTHER);
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayName() {
        return displayName;
    }
}
