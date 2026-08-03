package dev.clowiance.mickey.telegram.callback;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CallbackDispatcher {

    private final List<CallbackHandler> handlers;

    public CallbackDispatcher(List<CallbackHandler> handlers) {
        this.handlers = List.copyOf(handlers);
        validateNamespaces(this.handlers);
    }

    public Optional<CallbackView> dispatch(CallbackContext context) {
        return handlers.stream()
                .filter(handler -> handler.supports(context.data()))
                .findFirst()
                .map(handler -> handler.handle(context));
    }

    private void validateNamespaces(List<CallbackHandler> handlers) {
        for (int left = 0; left < handlers.size(); left++) {
            for (int right = left + 1; right < handlers.size(); right++) {
                for (String leftNamespace : handlers.get(left).namespaces()) {
                    for (String rightNamespace : handlers.get(right).namespaces()) {
                        if (leftNamespace.startsWith(rightNamespace) || rightNamespace.startsWith(leftNamespace)) {
                            throw new IllegalStateException("Callback namespace conflict: "
                                    + leftNamespace + " / " + rightNamespace);
                        }
                    }
                }
            }
        }
    }
}
