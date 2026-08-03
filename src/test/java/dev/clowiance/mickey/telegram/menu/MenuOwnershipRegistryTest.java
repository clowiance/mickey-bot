package dev.clowiance.mickey.telegram.menu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuOwnershipRegistryTest {

    private final MenuOwnershipRegistry registry = new MenuOwnershipRegistry();

    @Test
    void treatsMenuOfAnotherUserAsForeign() {
        registry.register(-100L, 42, 777L);

        assertThat(registry.isOwnedByOther(-100L, 42, 888L)).isTrue();
        assertThat(registry.isOwnedByOther(-100L, 42, 777L)).isFalse();
    }

    @Test
    void treatsUnknownMenuAsFreeToUse() {
        assertThat(registry.isOwnedByOther(-100L, 999, 888L)).isFalse();
        assertThat(registry.ownerOf(-100L, 999)).isEmpty();
    }

    @Test
    void separatesMenusOfDifferentChats() {
        registry.register(-100L, 42, 777L);
        registry.register(-200L, 42, 888L);

        assertThat(registry.ownerOf(-100L, 42)).contains(777L);
        assertThat(registry.ownerOf(-200L, 42)).contains(888L);
    }

    @Test
    void forgetsReplacedMenu() {
        registry.register(-100L, 42, 777L);
        registry.forget(-100L, 42);

        assertThat(registry.ownerOf(-100L, 42)).isEmpty();
    }

    @Test
    void ignoresIncompleteKeysInsteadOfFailing() {
        registry.register(null, 42, 777L);
        registry.register(-100L, null, 777L);
        registry.register(-100L, 42, null);

        assertThat(registry.ownerOf(-100L, 42)).isEmpty();
        assertThat(registry.isOwnedByOther(null, null, 777L)).isFalse();
    }

    @Test
    void dropsOldestMenusInsteadOfGrowingForever() {
        for (int messageId = 1; messageId <= 2100; messageId++) {
            registry.register(-100L, messageId, (long) messageId);
        }

        assertThat(registry.ownerOf(-100L, 1)).isEmpty();
        assertThat(registry.ownerOf(-100L, 2100)).contains(2100L);
    }
}
