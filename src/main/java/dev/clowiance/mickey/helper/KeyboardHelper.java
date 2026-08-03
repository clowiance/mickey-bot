package dev.clowiance.mickey.helper;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

<<<<<<< HEAD
<<<<<<< HEAD
import java.util.ArrayList;
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
import java.util.ArrayList;
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
import java.util.List;

@Component
public class KeyboardHelper {

    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardButton faq = button("❓ FAQ", "menu:faq");
        InlineKeyboardButton specialties = button("🎯 Спеціальності", "faq:specialties");
        InlineKeyboardButton prices = button("💰 Ціни", "faq:prices");
        InlineKeyboardButton admission = button("📝 Питання по вступу", "faq:admission");
        InlineKeyboardButton groups = button("👥 Чати груп", "groups:menu");
        InlineKeyboardButton dorms = button("🏠 Гуртожиток", "faq:dorms");
        InlineKeyboardButton help = button("🤖 Команди", "menu:help");

        return InlineKeyboardMarkup.builder()
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
                .keyboardRow(List.of(help))
                .keyboardRow(List.of(specialties, prices))
                .keyboardRow(List.of(admission))
                .keyboardRow(List.of(groups, dorms))
                .keyboardRow(List.of(faq))
<<<<<<< HEAD
=======
                .keyboardRow(List.of(faq))
                .keyboardRow(List.of(specialties, prices))
                .keyboardRow(List.of(dorms))
                .keyboardRow(List.of(help))
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
                .build();
    }

    public InlineKeyboardMarkup faqMenu() {
        InlineKeyboardButton specialties = button("🎯 Спеціальності", "faq:specialties");
        InlineKeyboardButton prices = button("💰 Ціни", "faq:prices");
        InlineKeyboardButton admission = button("📝 Питання по вступу", "faq:admission");
        InlineKeyboardButton groups = button("👥 Чати навчальних груп", "groups:menu");
        InlineKeyboardButton dorms = button("🏠 Гуртожиток", "faq:dorms");
        InlineKeyboardButton back = button("⬅️ Назад", "menu:main");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(specialties))
                .keyboardRow(List.of(prices))
                .keyboardRow(List.of(admission))
                .keyboardRow(List.of(groups))
                .keyboardRow(List.of(dorms))
                .keyboardRow(List.of(back))
                .build();
    }

    public InlineKeyboardMarkup backToMenu() {
        return InlineKeyboardMarkup.builder()
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
                .keyboardRow(List.of(button("⬅️ Назад", "menu:main")))
                .build();
    }

    public InlineKeyboardMarkup backToFaq() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⬅️ До FAQ", "menu:faq")))
                .keyboardRow(List.of(button("🏠 Головне меню", "menu:main")))
                .build();
    }

    public InlineKeyboardMarkup welcomeMemberNumber(long memberNumber) {
        InlineKeyboardButton counterButton = InlineKeyboardButton.builder()
                .text("👤 Учасник #" + memberNumber)
                .callbackData("welcome:member_number")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(counterButton))
                .build();
    }

    public InlineKeyboardMarkup logsMenu(Long chatId, String currentCategory, int page, boolean hasNext, boolean hasPrev) {
        String allMark = "ALL".equals(currentCategory) ? "🟢 Всі події" : "🔄 Всі події";
        String msgMark = "MESSAGE".equals(currentCategory) ? "🟢 Повідомлення" : "💬 Повідомлення";
        String modMark = "MODERATION".equals(currentCategory) ? "🟢 Модерація" : "🛡️ Модерація";

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (hasPrev) {
            navRow.add(button("⬅️", "logs_page:" + chatId + ":" + currentCategory + ":" + (page - 1)));
        }
        navRow.add(button("🔄", "logs_filter:" + chatId + ":" + currentCategory + ":" + page));
        if (hasNext) {
            navRow.add(button("➡️", "logs_page:" + chatId + ":" + currentCategory + ":" + (page + 1)));
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button(allMark, "logs_filter:" + chatId + ":ALL:0"),
                        button(msgMark, "logs_filter:" + chatId + ":MESSAGE:0"),
                        button(modMark, "logs_filter:" + chatId + ":MODERATION:0")
                ))
                .keyboardRow(navRow)
                .keyboardRow(List.of(
                        button("❌ Закрити аудит", "logs_close")
                ))
<<<<<<< HEAD
=======
                .keyboardRow(List.of(button("⬅️ Назад до меню", "menu:main")))
=======
                .build();
    }

    public InlineKeyboardMarkup catalogLevelMenu(String action) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("🎓 Бакалаврат", "catalog:" + action + ":bachelor"),
                        button("🎓 Магістратура", "catalog:" + action + ":master")))
                .keyboardRow(List.of(button("⬅️ Назад", "menu:main")))
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
                .build();
    }

    public InlineKeyboardMarkup devPanel() {
        InlineKeyboardButton reports = button("📋 Всі репорти", "dev:reports");
        InlineKeyboardButton chats = button("💬 Чати", "dev:chats");
<<<<<<< HEAD
=======
        InlineKeyboardButton groups = button("🧩 Керовані групи", "dev:groups");
        InlineKeyboardButton catalog = button("🎓 Каталог УКД", "dev:catalog");
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        InlineKeyboardButton stats = button("📊 Статистика", "dev:stats");
        InlineKeyboardButton back = button("⬅️ Назад", "menu:main");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(reports, chats))
<<<<<<< HEAD
=======
                .keyboardRow(List.of(groups, catalog))
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
                .keyboardRow(List.of(stats))
                .keyboardRow(List.of(back))
                .build();
    }

    public InlineKeyboardMarkup adminPanel() {
        InlineKeyboardButton reports = button("📋 Репорти чату", "admin:reports");
        InlineKeyboardButton stats = button("📊 Статистика чату", "admin:stats");
<<<<<<< HEAD
=======
        InlineKeyboardButton settings = button("⚙️ Налаштування групи", "admin:settings");
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        InlineKeyboardButton back = button("⬅️ Назад", "menu:main");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(reports))
                .keyboardRow(List.of(stats))
<<<<<<< HEAD
                .keyboardRow(List.of(back))
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
                .keyboardRow(List.of(settings))
                .keyboardRow(List.of(back))
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
