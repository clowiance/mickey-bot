package dev.clowiance.mickey.catalog.presentation;

import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CatalogMessageRenderer {

    private final UniversityCatalogService catalogService;

    public String renderSpecialties(EducationLevel level) {
        List<Specialty> specialties = catalogService.findActive(level);
        StringBuilder text = new StringBuilder("🎓 <b>")
                .append(level.getDisplayName())
                .append(" — спеціальності УКД</b>\n\n");
        for (Specialty specialty : specialties) {
            text.append("• <b>").append(escape(specialty.code())).append(" ")
                    .append(escape(specialty.name())).append("</b>");
            if (specialty.educationalProgram() != null && !specialty.educationalProgram().isBlank()
                    && !specialty.educationalProgram().equalsIgnoreCase(specialty.name())) {
                text.append("\n  ОП: ").append(escape(specialty.educationalProgram()));
            }
            text.append("\n");
        }
        text.append("\nДані перевірено за офіційним сайтом УКД у 2026 році.");
        return text.toString();
    }

    public String renderPrices(EducationLevel level) {
        List<Specialty> specialties = catalogService.findActive(level);
        StringBuilder text = new StringBuilder("💰 <b>Вартість навчання — ")
                .append(level.getDisplayName())
                .append(" (2026)</b>\n\n");
        for (Specialty specialty : specialties) {
            text.append("• <b>").append(escape(specialty.code())).append(" ")
                    .append(escape(specialty.name())).append("</b>\n")
                    .append("  ").append(specialty.tuitionPeriod().getDisplayName()).append(" — Денна: ")
                    .append(formatAmount(specialty.fullTimeTuition()))
                    .append(" | Заочна: ").append(formatAmount(specialty.partTimeTuition())).append("\n");
            if (specialty.tuitionNote() != null && !specialty.tuitionNote().isBlank()) {
                text.append("  <i>").append(escape(specialty.tuitionNote())).append("</i>\n");
            }
        }
        text.append("\nℹ️ Джерело: <a href=\"")
                .append(level == EducationLevel.BACHELOR
                        ? "https://ukd.edu.ua/bakalavrat"
                        : "https://ukd.edu.ua/mahistratura")
                .append("\">офіційний сайт УКД</a>.");
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
        NumberFormat formatter = NumberFormat.getIntegerInstance(Locale.forLanguageTag("uk-UA"));
        return formatter.format(amount) + " грн";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
