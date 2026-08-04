package dev.clowiance.mickey.analytics.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Види діаграм. Раніше їх назви жили рядками в трьох місцях — у whitelist команди
 * {@code /charts}, у switch рендерера й у callback-даних кнопок — і вже розійшлися:
 * кнопка пропонувала {@code global}, а команда мовчки підміняла його на {@code group}.
 *
 * @param globalScope діаграма не залежить від конкретного чату, тому кешується одна на всіх
 * @param developerOnly доступна лише розробникам: показує дані по всіх чатах бота
 */
public enum ChartDashboard {

    GROUP("group", "Зведення по групі", false, false),
    ACTIVITY("activity", "Активність за 30 днів", false, false),
    RATING("rating", "Рейтинг балансу крон", true, false),
    EARNED("earned", "Рейтинг заробітку", true, false),
    GLOBAL("global", "Глобальна статистика бота", true, true);

    private final String slug;
    private final String title;
    private final boolean globalScope;
    private final boolean developerOnly;

    ChartDashboard(String slug, String title, boolean globalScope, boolean developerOnly) {
        this.slug = slug;
        this.title = title;
        this.globalScope = globalScope;
        this.developerOnly = developerOnly;
    }

    public String slug() {
        return slug;
    }

    public String title() {
        return title;
    }

    public boolean globalScope() {
        return globalScope;
    }

    public boolean developerOnly() {
        return developerOnly;
    }

    /**
     * @return {@code Optional.empty()} для невідомого значення — викликач має сказати
     *         про це користувачу, а не підсунути іншу діаграму
     */
    public static Optional<ChartDashboard> fromSlug(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(dashboard -> dashboard.slug.equals(normalized))
                .findFirst();
    }

    /**
     * Перелік для підказки: розробникам показуємо все, адміністраторам — лише їхнє.
     */
    public static String usage(boolean developer) {
        StringBuilder text = new StringBuilder("📊 <b>Доступні діаграми</b>\n\n");
        for (ChartDashboard dashboard : values()) {
            if (dashboard.developerOnly && !developer) {
                continue;
            }
            text.append("• <code>/charts ").append(dashboard.slug).append("</code> — ")
                    .append(dashboard.title).append("\n");
        }
        text.append("\nБез аргументу — <code>/charts group</code>.");
        return text.toString();
    }
}
