package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.application.StudyGroupDirectory;
import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@lombok.RequiredArgsConstructor
public class StudyGroupMessageRenderer {

    private final StudyGroupDirectory directory;

    private static final String DIVIDER = "━━━━━━━━━━━━━━━━━━";

    public String renderAll() {
        StringBuilder text = new StringBuilder();
        text.append("🎓 <b>ЧАТИ ГРУП УНІВЕРСИТЕТУ</b>\n")
                .append("<i>").append(groupWord(all().size())).append("</i>\n");

        for (Map.Entry<String, List<StudyGroup>> byCourse : groupByCourse(all()).entrySet()) {
            text.append("\n<b>").append(escape(byCourse.getKey())).append("</b>\n");
            for (StudyGroup group : byCourse.getValue()) {
                text.append("   • <a href=\"").append(group.inviteLink()).append("\">")
                        .append(escape(group.specialty())).append("</a>\n");
            }
        }

        text.append(DIVIDER).append("\n")
                .append("ℹ️ Посилання відкриває заявку на вступ у закритий чат групи.");
        return text.toString();
    }

    private List<StudyGroup> all() {
        return directory.findActive().stream()
                .filter(group -> group.category() == StudyGroupCategory.UNIVERSITY)
                .toList();
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
