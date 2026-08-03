package dev.clowiance.mickey.telegram.menu;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Пам'ятає, хто відкрив конкретне меню. Інлайн-клавіатура в Telegram спільна для всього
 * чату, тому без цього перемикання розділів одним учасником змінювало б картинку всім.
 * Дані живуть лише в пам'яті: меню ефемерні, після рестарту бот просто вважає їх нічийними.
 */
@Component
public class MenuOwnershipRegistry {

    private static final int MAX_TRACKED_MENUS = 2000;

    private final Map<String, Long> owners = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                    return size() > MAX_TRACKED_MENUS;
                }
            });

    public void register(Long chatId, Integer messageId, Long ownerId) {
        if (chatId == null || messageId == null || ownerId == null) {
            return;
        }
        owners.put(key(chatId, messageId), ownerId);
    }

    public Optional<Long> ownerOf(Long chatId, Integer messageId) {
        if (chatId == null || messageId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(owners.get(key(chatId, messageId)));
    }

    /**
     * Меню, яке бот видаляє й надсилає заново, більше не існує — прибираємо, щоб мапа
     * не тримала мертві id.
     */
    public void forget(Long chatId, Integer messageId) {
        if (chatId == null || messageId == null) {
            return;
        }
        owners.remove(key(chatId, messageId));
    }

    public boolean isOwnedByOther(Long chatId, Integer messageId, Long userId) {
        return ownerOf(chatId, messageId)
                .filter(owner -> !owner.equals(userId))
                .isPresent();
    }

    /**
     * Перемальовувати меню на місці можна лише тому, за ким воно закріплене. Меню без
     * власника (надіслані до рестарту або повз {@link MenuMessenger}) свідомо вважаються
     * чужими: інакше перший, хто натисне кнопку, змінював би картинку всьому чату.
     */
    public boolean isOwnedBy(Long chatId, Integer messageId, Long userId) {
        return ownerOf(chatId, messageId)
                .filter(owner -> owner.equals(userId))
                .isPresent();
    }

    private String key(Long chatId, Integer messageId) {
        return chatId + ":" + messageId;
    }
}
