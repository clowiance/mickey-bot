package dev.clowiance.mickey.catalog.presentation;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.telegram.callback.CallbackContext;
import dev.clowiance.mickey.telegram.callback.CallbackHandler;
import dev.clowiance.mickey.telegram.callback.CallbackView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogCallbackHandler implements CallbackHandler {

    private final CatalogMessageRenderer renderer;

    @Override
    public boolean supports(String callbackData) {
        return "faq:specialties".equals(callbackData)
                || "faq:prices".equals(callbackData)
                || callbackData.startsWith("catalog:");
    }

    @Override
    public CallbackView handle(CallbackContext context) {
        String data = context.data();
        if ("faq:specialties".equals(data) || "catalog:specialties:menu".equals(data)) {
            return levelMenu("🎯 <b>Спеціальності УКД</b>\n\nОберіть освітній рівень:", "specialties");
        }
        if ("faq:prices".equals(data) || "catalog:prices:menu".equals(data)) {
            return levelMenu("💰 <b>Вартість навчання в УКД — 2026</b>\n\nОберіть освітній рівень:", "prices");
        }

        String[] parts = data.split(":");
        String action = parts.length > 1 ? parts[1] : "specialties";
        EducationLevel level = EducationLevel.fromSlug(parts.length > 2 ? parts[2] : "bachelor");
        String text = "prices".equals(action)
                ? renderer.renderPrices(level)
                : renderer.renderSpecialties(level);
        return new CallbackView(text, backToLevelMenu(action));
    }

    private CallbackView levelMenu(String text, String action) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        button("🎓 Бакалаврат", "catalog:" + action + ":bachelor"),
                        button("🎓 Магістратура", "catalog:" + action + ":master")))
                .keyboardRow(List.of(button("⬅️ Назад", "menu:main")))
                .build();
        return new CallbackView(text, keyboard);
    }

    private InlineKeyboardMarkup backToLevelMenu(String action) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⬅️ Обрати рівень", "catalog:" + action + ":menu")))
                .keyboardRow(List.of(button("🏠 Головне меню", "menu:main")))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
    }
}
