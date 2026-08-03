package dev.clowiance.mickey.telegram.callback;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallbackDispatcherTest {

    @Test
    void rejectsOverlappingNamespacesAtStartup() {
        CallbackHandler broad = handler(Set.of("admin:"));
        CallbackHandler nested = handler(Set.of("admin:stats"));

        assertThatThrownBy(() -> new CallbackDispatcher(List.of(broad, nested)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Callback namespace conflict");
    }

    private CallbackHandler handler(Set<String> namespaces) {
        return new CallbackHandler() {
            @Override public Set<String> namespaces() { return namespaces; }
            @Override public boolean supports(String callbackData) { return false; }
            @Override public CallbackView handle(CallbackContext context) { return null; }
        };
    }
}
