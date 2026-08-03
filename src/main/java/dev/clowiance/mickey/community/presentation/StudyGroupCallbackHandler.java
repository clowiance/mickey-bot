package dev.clowiance.mickey.community.presentation;

import dev.clowiance.mickey.telegram.callback.CallbackContext;
import dev.clowiance.mickey.telegram.callback.CallbackHandler;
import dev.clowiance.mickey.telegram.callback.CallbackView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
        return new CallbackView(renderer.renderAll(), navigationMenu());
    }

    public InlineKeyboardMarkup navigationMenu() {
        return InlineKeyboardMarkup.builder()
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
