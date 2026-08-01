package dev.clowiance.mickey.telegram.callback;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public record CallbackView(String text, InlineKeyboardMarkup keyboard) {
}
