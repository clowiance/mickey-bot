package dev.clowiance.mickey.helper;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
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
                .keyboardRow(List.of(help))
                .keyboardRow(List.of(specialties, prices))
                .keyboardRow(List.of(dorms))
                .keyboardRow(List.of(faq))
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
                .keyboardRow(List.of(button("⬅️ Назад", "menu:main")))
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
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
