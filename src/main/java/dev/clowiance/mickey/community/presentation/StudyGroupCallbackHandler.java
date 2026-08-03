package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.domain.StudyGroupCategory;
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
public class StudyGroupCallbackHandler implements CallbackHandler {

    private final StudyGroupMessageRenderer renderer;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("groups:");
    }

    @Override
    public CallbackView handle(CallbackContext context) {
        return switch (context.data()) {
            case "groups:university" -> new CallbackView(
                    renderer.renderCategory(StudyGroupCategory.UNIVERSITY), backToGroupsMenu());
            case "groups:college" -> new CallbackView(
                    renderer.renderCategory(StudyGroupCategory.COLLEGE), backToGroupsMenu());
            case "groups:all" -> new CallbackView(renderer.renderAll(), backToGroupsMenu());
            default -> new CallbackView(renderer.renderMenu(), categoryMenu());
        };
    }

    public InlineKeyboardMarkup categoryMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("🎓 Університет (УНІК) — "
                        + renderer.countOf(StudyGroupCategory.UNIVERSITY), "groups:university")))
                .keyboardRow(List.of(button("🏫 Фаховий коледж — "
                        + renderer.countOf(StudyGroupCategory.COLLEGE), "groups:college")))
                .keyboardRow(List.of(button("📋 Показати всі групи", "groups:all")))
                .keyboardRow(List.of(button("🏠 Головне меню", "menu:main")))
                .build();
    }

    private InlineKeyboardMarkup backToGroupsMenu() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⬅️ До вибору", "groups:menu")))
                .keyboardRow(List.of(button("🏠 Головне меню", "menu:main")))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
    }
}
