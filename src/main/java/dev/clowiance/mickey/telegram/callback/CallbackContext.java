package dev.clowiance.mickey.telegram.callback;

public record CallbackContext(
        String data,
        Long chatId,
        Long userId,
        boolean developer,
        boolean chatAdmin
) {
}
