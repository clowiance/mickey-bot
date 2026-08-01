package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.bootstrap.UkdStudyGroupData;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class StudyGroupMessageRenderer {

    private static final String DIVIDER = "━━━━━━━━━━━━━━━━━━";

    public String renderMenu() {
        return "👥 <b>ЧАТИ НАВЧАЛЬНИХ ГРУП</b>\n"
                + "<i>Усього груп: " + all().size() + "</i>\n"
                + DIVIDER + "\n"
                + "🎓 Університет (УНІК) — " + countOf(StudyGroupCategory.UNIVERSITY) + "\n"
                + "🏫 Фаховий коледж — " + countOf(StudyGroupCategory.COLLEGE) + "\n"
                + unspecifiedLine()
                + "\nОбери, де ти навчаєшся 👇\n\n"
                + "<i>Це закриті чати одногрупників — посилання відкриває заявку на вступ у групу.</i>";
    }

    public String renderCategory(StudyGroupCategory category) {
        List<StudyGroup> groups = all().stream()
                .filter(group -> group.category() == category)
                .toList();

        StringBuilder text = new StringBuilder();
        text.append(category.getEmoji()).append(" <b>ЧАТИ ГРУП — ")
                .append(category.getDisplayName().toUpperCase(java.util.Locale.forLanguageTag("uk-UA")))
                .append("</b>\n")
                .append("<i>").append(groupWord(groups.size())).append("</i>\n");

        // Групуємо за курсом: «1 курс» і «Після 9 класу» — різні набори людей,
        // і плутати їх в одному списку означає гарантовані заявки не в той чат.
        for (Map.Entry<String, List<StudyGroup>> byCourse : groupByCourse(groups).entrySet()) {
            text.append("\n<b>").append(escape(byCourse.getKey())).append("</b>\n");
            for (StudyGroup group : byCourse.getValue()) {
                text.append("   • <a href=\"").append(group.inviteLink()).append("\">")
                        .append(escape(group.specialty())).append("</a>\n");
            }
        }

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
                    .sorted(sortOrder())
                    .toList();
            if (groups.isEmpty()) {
                continue;
            }
            text.append("\n").append(category.getEmoji()).append(" <b>")
                    .append(escape(category.getDisplayName())).append("</b>\n");
            for (StudyGroup group : groups) {
                text.append("   • <a href=\"").append(group.inviteLink()).append("\">")
                        .append(escape(group.specialty())).append("</a>");
                if (group.hasCourse()) {
                    text.append(" — <i>").append(escape(group.course())).append("</i>");
                }
                text.append("\n");
            }
        }

        text.append(DIVIDER).append("\n")
                .append("ℹ️ Посилання відкриває заявку на вступ у закритий чат групи.");
        return text.toString();
    }

    /**
     * Рядок з'являється, лише поки в таблиці є групи без категорії — інакше підсумок
     * угорі не сходився б із двома кнопками вибору.
     */
    private String unspecifiedLine() {
        int count = countOf(StudyGroupCategory.UNSPECIFIED);
        return count == 0 ? "" : "❔ Без категорії — " + count + " (у списку «всі групи»)\n";
    }

    public int countOf(StudyGroupCategory category) {
        return (int) all().stream().filter(group -> group.category() == category).count();
    }

    private List<StudyGroup> all() {
        return UkdStudyGroupData.entries();
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
            case "Після 9 класу" -> 3;
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
