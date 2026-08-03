package dev.clowiance.mickey.telegram.callback;

import java.util.Set;

public interface CallbackHandler {
    Set<String> namespaces();

    boolean supports(String callbackData);

    CallbackView handle(CallbackContext context);
}
