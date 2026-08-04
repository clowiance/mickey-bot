package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.community.domain.StudyGroupCategory;
import dev.clowiance.mickey.telegram.callback.CallbackContext;
import dev.clowiance.mickey.telegram.callback.CallbackHandler;
import dev.clowiance.mickey.telegram.callback.CallbackView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        String action = context.data().substring("groups:".length());

        if ("all".equals(action)) {
            return new CallbackView(renderer.renderAll(), backToGroups());
        }

        StudyGroupCategory category = StudyGroupCategory.fromSlug(action);
        if (category != null) {
            return new CallbackView(renderer.renderCategory(category), backToGroups());
        }

        return new CallbackView(renderer.renderMenu(), navigationMenu());
    }

    /**
     * Кнопки категорій. Порожні категорії не показуємо, щоб не вести в нікуди.
     */
    public InlineKeyboardMarkup navigationMenu() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (StudyGroupCategory category : StudyGroupCategory.values()) {
            if (renderer.countOf(category) == 0) {
                continue;
            }
            rows.add(List.of(button(category.getEmoji() + " " + category.getDisplayName(),
                    "groups:" + category.slug())));
        }
        rows.add(List.of(button("📋 Показати всі", "groups:all")));
        rows.add(List.of(button("🏠 Головне меню", "menu:main")));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private InlineKeyboardMarkup backToGroups() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button("⬅️ До вибору категорії", "groups:menu")))
                .keyboardRow(List.of(button("🏠 Головне меню", "menu:main")))
                .build();
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
    }

    @Override
    public Set<String> namespaces() {
        return Set.of("groups:");
    }
}
