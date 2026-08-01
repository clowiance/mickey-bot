package dev.clowiance.mickey.telegram.callback;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CallbackDispatcher {

    private final List<CallbackHandler> handlers;

    public CallbackDispatcher(List<CallbackHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public Optional<CallbackView> dispatch(CallbackContext context) {
        return handlers.stream()
                .filter(handler -> handler.supports(context.data()))
                .findFirst()
                .map(handler -> handler.handle(context));
    }
}
