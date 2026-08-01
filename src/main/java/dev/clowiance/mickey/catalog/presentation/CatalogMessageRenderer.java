package dev.clowiance.mickey.catalog.presentation;

import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CatalogMessageRenderer {

    private static final Locale UA = Locale.forLanguageTag("uk-UA");
    private static final String DIVIDER = "━━━━━━━━━━━━━━━━━━";

    private final UniversityCatalogService catalogService;

    public String renderSpecialties(EducationLevel level) {
        List<Specialty> specialties = catalogService.findActive(level);
        StringBuilder text = new StringBuilder();
        text.append("🎓 <b>").append(level.getDisplayName().toUpperCase(UA))
                .append(" — СПЕЦІАЛЬНОСТІ УКД</b>\n")
                .append("<i>2026 рік · ").append(programWord(specialties.size())).append("</i>\n")
                .append(DIVIDER).append("\n");

        for (Specialty specialty : specialties) {
            text.append("<code>").append(escape(specialty.code())).append("</code> <b>")
                    .append(escape(specialty.name())).append("</b>\n");
            if (hasDistinctProgram(specialty)) {
                text.append("      ↳ <i>").append(escape(specialty.educationalProgram())).append("</i>\n");
            }
        }

        text.append(DIVIDER).append("\n")
                .append("💰 Вартість навчання — /prices\n")
                .append("🔗 <a href=\"").append(sourceUrl(level)).append("\">Офіційний сайт УКД</a>");
        return text.toString();
    }

    public String renderPrices(EducationLevel level) {
        List<Specialty> specialties = catalogService.findActive(level);
        StringBuilder text = new StringBuilder();
        text.append("💰 <b>ВАРТІСТЬ НАВЧАННЯ — ")
                .append(level.getDisplayName().toUpperCase(UA)).append("</b>\n")
                .append("<i>").append(periodCaption(specialties)).append(" · 2026 рік</i>\n");

        String range = fullTimeRange(specialties);
        if (range != null) {
            text.append("<i>Денна форма: ").append(range).append("</i>\n");
        }
        text.append(DIVIDER).append("\n");

        for (Specialty specialty : specialties) {
            text.append("<b>").append(escape(specialty.name())).append("</b> <code>")
                    .append(escape(specialty.code())).append("</code>\n");
            // Кілька програм ділять один код (напр. три «Менеджмент D3»), тому без назви ОП
            // рядки виглядали б однаково.
            if (hasDistinctProgram(specialty)) {
                text.append("      ↳ <i>").append(escape(specialty.educationalProgram())).append("</i>\n");
            }
            if (specialty.fullTimeTuition() == null && specialty.partTimeTuition() == null) {
                text.append("      💵 Вартість: уточнюйте в приймальній комісії\n");
            } else {
                text.append("      💵 Денна: ").append(formatAmount(specialty.fullTimeTuition())).append("\n")
                        .append("      📘 Заочна: ").append(formatPartTime(specialty.partTimeTuition())).append("\n");
            }
            if (specialty.tuitionNote() != null && !specialty.tuitionNote().isBlank()) {
                text.append("      ℹ️ <i>").append(escape(specialty.tuitionNote())).append("</i>\n");
            }
        }

        text.append(DIVIDER).append("\n")
                .append("💳 Оплата можлива частинами — за семестрами або щомісяця\n")
                .append("🎯 Перелік спеціальностей — /specialties\n")
                .append("🔗 <a href=\"").append(sourceUrl(level)).append("\">Офіційний сайт УКД</a>");
        return text.toString();
    }

    public String renderAdminList(EducationLevel level) {
        StringBuilder text = new StringBuilder("🧾 <b>Каталог для адміністрування — ")
                .append(level.getDisplayName()).append("</b>\n\n");
        for (Specialty specialty : catalogService.findAll(level)) {
            text.append(Boolean.TRUE.equals(specialty.active()) ? "✅ " : "⛔ ")
                    .append("<code>").append(specialty.id()).append("</code> — ")
                    .append(escape(specialty.code())).append(" ")
                    .append(escape(specialty.name())).append("\n");
        }
        return text.toString();
    }

    public String formatAmount(Long amount) {
        if (amount == null) {
            return "не опубліковано";
        }
        NumberFormat formatter = NumberFormat.getIntegerInstance(UA);
        return formatter.format(amount) + " грн";
    }

    private String formatPartTime(Long amount) {
        return amount == null ? "не передбачена" : formatAmount(amount);
    }

    /**
     * Період оплати однаковий для більшості програм рівня, тому виносимо його в підзаголовок.
     * Якщо в межах рівня періоди різні (стоматологія рахується за І курс), лишаємо загальне формулювання.
     */
    private String periodCaption(List<Specialty> specialties) {
        List<String> periods = distinctPeriods(specialties);
        return periods.size() == 1 ? "Ціна за " + periods.get(0) : "Ціна за період навчання";
    }

    private List<String> distinctPeriods(List<Specialty> specialties) {
        return specialties.stream()
                .map(Specialty::tuitionPeriod)
                .filter(Objects::nonNull)
                .map(period -> period.getDisplayName())
                .distinct()
                .toList();
    }

    /**
     * Діапазон має сенс лише коли всі програми рівня рахуються за один період: інакше
     * ціна за І курс (стоматологія) стояла б поруч із ціною за 1,5 року.
     */
    private String fullTimeRange(List<Specialty> specialties) {
        if (distinctPeriods(specialties).size() != 1) {
            return null;
        }
        List<Long> amounts = specialties.stream()
                .map(Specialty::fullTimeTuition)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        if (amounts.isEmpty()) {
            return null;
        }
        Long min = amounts.get(0);
        Long max = amounts.get(amounts.size() - 1);
        return min.equals(max) ? formatAmount(min) : formatAmount(min) + " – " + formatAmount(max);
    }

    private String programWord(int count) {
        int lastTwo = count % 100;
        int last = count % 10;
        String word;
        if (lastTwo >= 11 && lastTwo <= 14) {
            word = "програм";
        } else if (last == 1) {
            word = "програма";
        } else if (last >= 2 && last <= 4) {
            word = "програми";
        } else {
            word = "програм";
        }
        return count + " " + word;
    }

    /**
     * Показуємо назву ОП лише тоді, коли вона додає інформації. «Туризм та рекреація» /
     * «Туризм і рекреація» чи «Готельно-ресторанна справа» всередині назви спеціальності —
     * це той самий текст, і дублювати його в списку немає сенсу.
     */
    private boolean hasDistinctProgram(Specialty specialty) {
        String program = specialty.educationalProgram();
        if (program == null || program.isBlank()) {
            return false;
        }
        String normalizedProgram = normalizeForComparison(program);
        String normalizedName = normalizeForComparison(specialty.name());
        // Ховаємо ОП лише коли вона повністю вкладається в назву спеціальності. Зворотний
        // напрямок («Дизайн» → «Графічний дизайн») навпаки уточнює програму, тож лишається.
        return !normalizedName.contains(normalizedProgram);
    }

    private String normalizeForComparison(String value) {
        return value.toLowerCase(UA)
                .replace(" та ", " ")
                .replace(" і ", " ")
                .replace(",", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String sourceUrl(EducationLevel level) {
        return level == EducationLevel.BACHELOR
                ? "https://ukd.edu.ua/bakalavrat"
                : "https://ukd.edu.ua/mahistratura";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
