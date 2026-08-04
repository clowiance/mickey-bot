package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.application.StudyGroupDirectory;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@lombok.RequiredArgsConstructor
public class StudyGroupMessageRenderer {

    private final StudyGroupDirectory directory;

    private static final String DIVIDER = "━━━━━━━━━━━━━━━━━━";

    /** Українська локаль: без неї «і» у верхньому регістрі перетворюється не на ту літеру. */
    private static final Locale UA = Locale.forLanguageTag("uk-UA");

    /**
     * Стартовий екран: скільки груп у кожній категорії. Далі людина обирає свою —
     * так університет і коледж не змішуються в одному списку.
     */
    public String renderMenu() {
        StringBuilder text = new StringBuilder();
        text.append("👥 <b>ЧАТИ НАВЧАЛЬНИХ ГРУП</b>\n")
                .append("<i>").append(groupWord(all().size())).append("</i>\n")
                .append(DIVIDER).append("\n");

        for (StudyGroupCategory category : StudyGroupCategory.values()) {
            long count = countOf(category);
            if (count == 0) {
                continue;
            }
            text.append(category.getEmoji()).append(" ").append(escape(category.getDisplayName()))
                    .append(" — ").append(count).append("\n");
        }

        text.append("\nОбери, де ти навчаєшся 👇\n\n")
                .append("<i>Це закриті чати одногрупників — посилання відкриває заявку на вступ у групу.</i>");
        return text.toString();
    }

    public String renderCategory(StudyGroupCategory category) {
        List<StudyGroup> groups = all().stream()
                .filter(group -> group.category() == category)
                .toList();

        StringBuilder text = new StringBuilder();
        text.append(category.getEmoji()).append(" <b>ЧАТИ ГРУП — ")
                .append(escape(category.getDisplayName()).toUpperCase(UA))
                .append("</b>\n")
                .append("<i>").append(groupWord(groups.size())).append("</i>\n");

        if (groups.isEmpty()) {
            text.append("\nПоки що жодного чату в цій категорії.\n");
        }
        appendByCourse(text, groups);

        text.append(DIVIDER).append("\n")
                .append("ℹ️ Не знайшов свою групу — напиши кураторам або адміністрації чату.");
        return text.toString();
    }

    public String renderAll() {
        StringBuilder text = new StringBuilder();
        text.append("👥 <b>УСІ ЧАТИ НАВЧАЛЬНИХ ГРУП</b>\n")
                .append("<i>").append(groupWord(all().size())).append("</i>\n");

        for (StudyGroupCategory category : StudyGroupCategory.values()) {
            List<StudyGroup> groups = all().stream()
                    .filter(group -> group.category() == category)
                    .toList();
            if (groups.isEmpty()) {
                continue;
            }
            text.append("\n").append(category.getEmoji()).append(" <b>")
                    .append(escape(category.getDisplayName())).append("</b>\n");
            appendByCourse(text, groups);
        }

        text.append(DIVIDER).append("\n")
                .append("ℹ️ Посилання відкриває заявку на вступ у закритий чат групи.");
        return text.toString();
    }

    private void appendByCourse(StringBuilder text, List<StudyGroup> groups) {
        // «1 курс» і «Після 9 класу» — різні набори людей, тому курс лишається видимим.
        for (Map.Entry<String, List<StudyGroup>> byCourse : groupByCourse(groups).entrySet()) {
            text.append("\n<b>").append(escape(byCourse.getKey())).append("</b>\n");
            for (StudyGroup group : byCourse.getValue()) {
                text.append("   • <a href=\"").append(group.inviteLink()).append("\">")
                        .append(escape(group.specialty())).append("</a>\n");
            }
        }
    }

    public long countOf(StudyGroupCategory category) {
        return all().stream().filter(group -> group.category() == category).count();
    }

    private List<StudyGroup> all() {
        return directory.findActive();
    }

    /**
     * Групи без вказаного курсу зводимо в окремий блок наприкінці, щоб не видавати
     * невідомий курс за конкретний.
     */
    private Map<String, List<StudyGroup>> groupByCourse(List<StudyGroup> groups) {
        return groups.stream().collect(Collectors.groupingBy(
                group -> group.hasCourse() ? group.course() : "Курс не вказано",
                () -> new TreeMap<>(courseOrder()),
                Collectors.collectingAndThen(Collectors.toList(),
                        list -> list.stream().sorted(sortOrder()).toList())));
    }

    private Comparator<String> courseOrder() {
        return Comparator.comparingInt((String course) -> switch (course) {
            case "1 курс" -> 1;
            case "2 курс" -> 2;
            case "3 курс" -> 3;
            case "Після 11 класу" -> 4;
            case "Після 9 класу" -> 5;
            default -> 9;
        }).thenComparing(Comparator.naturalOrder());
    }

    private Comparator<StudyGroup> sortOrder() {
        return Comparator.comparing(StudyGroup::specialty, String.CASE_INSENSITIVE_ORDER);
    }

    private String groupWord(int count) {
        int lastTwo = count % 100;
        int last = count % 10;
        String word;
        if (lastTwo >= 11 && lastTwo <= 14) {
            word = "груп";
        } else if (last == 1) {
            word = "група";
        } else if (last >= 2 && last <= 4) {
            word = "групи";
        } else {
            word = "груп";
        }
        return count + " " + word;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
