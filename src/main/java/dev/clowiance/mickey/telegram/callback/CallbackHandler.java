package dev.clowiance.mickey.telegram.callback;

public interface CallbackHandler {
    boolean supports(String callbackData);

    CallbackView handle(CallbackContext context);
}
