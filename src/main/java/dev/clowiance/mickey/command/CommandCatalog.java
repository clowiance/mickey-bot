package dev.clowiance.mickey.command;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Єдине джерело правди для опису команд. Раніше перелік жив копією в {@code TextHelper.HELP_TEXT}
 * і розходився з реальністю: /help рекламував /adminpanel і /devpanel, яких у коді вже немає,
 * і мовчав про /charts, /groupadmin, /economy та /chat. Тепер і текст довідки, і синє меню
 * команд Telegram будуються з цієї мапи, а рівень доступу береться з самого біна команди,
 * тому розійтися їм нема з чим.
 */
public final class CommandCatalog {

    private CommandCatalog() {
    }

    /**
     * Розділи довідки. Порядок оголошення визначає порядок секцій у /help.
     */
    public enum Section {
        BASIC("📋 <b>ОСНОВНІ КОМАНДИ</b>"),
        CATALOG("🎓 <b>КАТАЛОГ УКД ТА СПІЛЬНОТА</b>"),
        ECONOMY("💰 <b>ПРОФІЛЬ ТА ЕКОНОМІКА</b>"),
        INTERACTIVE("📊 <b>ІНТЕРАКТИВНІ КОМАНДИ</b>"),
        ADMIN("🛠️ <b>КЕРУВАННЯ ЧАТОМ</b>"),
        MODERATION("👮 <b>МОДЕРАЦІЯ ТА ПОКАРАННЯ</b>"),
        DEVELOPER("🔧 <b>КОМАНДИ РОЗРОБНИКА</b>");

        private final String title;

        Section(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }

    /**
     * @param usage       рядок із аргументами для /help
     * @param description короткий опис; Telegram обмежує його 256 символами
     * @param section     розділ довідки
     */
    public record Entry(String usage, String description, Section section) {
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    static {
        put("/start", "/start", "розпочати роботу та відкрити головне меню", Section.BASIC);
        put("/menu", "/menu", "відкрити головне меню бота", Section.BASIC);
        put("/help", "/help", "перелік доступних тобі команд", Section.BASIC);

        put("/specialties", "/specialties", "каталог спеціальностей УКД", Section.CATALOG);
        put("/prices", "/prices", "офіційна вартість навчання УКД", Section.CATALOG);
        put("/groups", "/groups", "чати навчальних груп університету", Section.CATALOG);

        put("/profile", "/profile [username або ID]", "профіль користувача (без аргументів — свій)", Section.ECONOMY);
        put("/balance", "/balance", "переглянути власний баланс крон", Section.ECONOMY);
        put("/daily", "/daily", "отримати щоденний бонус крон", Section.ECONOMY);
        put("/tops", "/tops [категорія]", "топ-10 користувачів за категорією", Section.ECONOMY);

        put("/poll", "/poll [Питання?] [Варіант1];[Варіант2]", "створити опитування в чаті", Section.INTERACTIVE);
        put("/report", "/report [username або ID] [причина]", "конфіденційно поскаржитись на порушника", Section.INTERACTIVE);
        put("/schedule", "/schedule [РРРР-ММ-ДД] [ГГ:ХХ] [Текст]", "запланувати розсилку (лише в ЛС бота)", Section.INTERACTIVE);

        put("/admin", "/admin", "панель керування чатом", Section.ADMIN);
        put("/chat", "/chat", "інформація та налаштування чату", Section.ADMIN);
        put("/logs", "/logs", "аудит подій та активності чату", Section.ADMIN);
        put("/reports", "/reports", "список поданих скарг", Section.ADMIN);
        put("/charts", "/charts", "діаграми активності чату", Section.ADMIN);
        put("/clear", "/clear [1-100]", "видалити останні N повідомлень", Section.ADMIN);
        put("/stats", "/stats [username або ID]", "статистика активності учасника", Section.ADMIN);

        put("/warn", "/warn [username або ID] [причина]", "видати попередження", Section.MODERATION);
        put("/unwarn", "/unwarn [username або ID]", "зняти одне попередження", Section.MODERATION);
        put("/mute", "/mute [username або ID] [тривалість] [причина]", "заглушити (напр. 30min, 2h, perm)", Section.MODERATION);
        put("/unmute", "/unmute [username або ID]", "зняти мут", Section.MODERATION);
        put("/kick", "/kick [username або ID] [причина]", "вигнати без блокування", Section.MODERATION);
        put("/ban", "/ban [username або ID] [тривалість] [причина]", "заблокувати (напр. 7d, perm)", Section.MODERATION);
        put("/unban", "/unban [ID]", "розбанити за Telegram ID", Section.MODERATION);

        put("/dev", "/dev", "панель розробника", Section.DEVELOPER);
        put("/economy", "/economy [дія] [username/ID] [сума]", "керування економікою", Section.DEVELOPER);
        put("/catalogadmin", "/catalogadmin", "керування каталогом УКД", Section.DEVELOPER);
        put("/groupadmin", "/groupadmin", "керування чатами навчальних груп", Section.DEVELOPER);
        put("/broadcastall", "/broadcastall [текст]", "розсилка по керованих групах", Section.DEVELOPER);
    }

    private static void put(String command, String usage, String description, Section section) {
        ENTRIES.put(command, new Entry(usage, description, section));
    }

    public static Optional<Entry> find(String command) {
        return Optional.ofNullable(ENTRIES.get(command));
    }

    public static Map<String, Entry> entries() {
        return Map.copyOf(ENTRIES);
    }
}
