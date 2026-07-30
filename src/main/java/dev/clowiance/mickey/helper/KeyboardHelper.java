package dev.clowiance.mickey.helper;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class KeyboardHelper {

    public InlineKeyboardMarkup mainMenu() {
        InlineKeyboardButton faq = button("❓ FAQ", "menu:faq");
        InlineKeyboardButton specialties = button("🎯 Спеціальності", "faq:specialties");
        InlineKeyboardButton prices = button("💰 Ціни", "faq:prices");
        InlineKeyboardButton dorms = button("🏠 Гуртожитки", "faq:dorms");
        InlineKeyboardButton help = button("🤖 Команди", "menu:help");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(faq))
                .keyboardRow(List.of(specialties, prices))
                .keyboardRow(List.of(dorms))
                .keyboardRow(List.of(help))
                .build();
    }

    public InlineKeyboardMarkup faqMenu() {
        InlineKeyboardButton specialties = button("🎯 Спеціальності", "faq:specialties");
        InlineKeyboardButton prices = button("💰 Ціни", "faq:prices");
        InlineKeyboardButton dorms = button("🏠 Гуртожитки", "faq:dorms");
        InlineKeyboardButton back = button("⬅️ Назад", "menu:main");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(specialties))
                .keyboardRow(List.of(prices))
                .keyboardRow(List.of(dorms))
                .keyboardRow(List.of(back))
                .build();
    }

    public InlineKeyboardMarkup backToMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⬅️ Назад до меню", "menu:main")))
                .build();
    }

    public InlineKeyboardMarkup devPanel() {
        InlineKeyboardButton reports = button("📋 Всі репорти", "dev:reports");
        InlineKeyboardButton chats = button("💬 Чати", "dev:chats");
        InlineKeyboardButton stats = button("📊 Статистика", "dev:stats");
        InlineKeyboardButton back = button("⬅️ Назад", "menu:main");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(reports, chats))
                .keyboardRow(List.of(stats))
                .keyboardRow(List.of(back))
                .build();
    }

    public InlineKeyboardMarkup adminPanel() {
        InlineKeyboardButton reports = button("📋 Репорти чату", "admin:reports");
        InlineKeyboardButton stats = button("📊 Статистика чату", "admin:stats");
        InlineKeyboardButton back = button("⬅️ Назад", "menu:main");

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(reports))
                .keyboardRow(List.of(stats))
                .keyboardRow(List.of(back))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
