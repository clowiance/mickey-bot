# Архітектура

Нові функції реалізовані модульно, щоб поступово винести старий package-by-layer код без ризикового повного переписування.

## Каталог УКД

```text
catalog/domain
  Specialty, EducationLevel, StudyForm, TuitionPeriod
        ↓
catalog/application
  UniversityCatalogService → SpecialtyCatalogPort
        ↓
catalog/infrastructure/persistence
  JpaSpecialtyCatalogAdapter → Spring Data JPA → SQLite
        ↓
catalog/presentation
  Telegram renderer і callback handler
```

Домен не знає про Telegram чи JPA. Application-сервіс працює через порт, а конкретна база даних підключена адаптером.

## Керування групами

```text
administration/domain
  ManagedChatSettings, ChatAudience, ChatFeature
        ↓
administration/application
  ChatAdministrationService → ManagedChatSettingsPort
  GroupBroadcastService
        ↓
administration/infrastructure/persistence
  JpaManagedChatSettingsAdapter → SQLite
        ↓
administration/presentation
  AdminSettingsCallbackHandler, WelcomeMessageRenderer
```

Один екземпляр бота підтримує необмежену кількість чатів. Налаштування і спеціальність зберігаються за Telegram `chatId`.

Стан учасника (`warn`, `mute`, `ban`) зберігається в `group_member_states` за складеним ключем `chatId + userId`. Покарання в одній групі не впливають на іншу. Старі глобальні значення з таблиці `chat_members` автоматично не переносяться, бо з них неможливо достовірно визначити групу.

## Telegram callbacks

`CallbackDispatcher` залежить від списку реалізацій `CallbackHandler`. Нові розділи додаються окремим handler-класом без розширення центрального `switch` у `MickeyHandler`. Старі callback-и залишені як сумісний контур і можуть мігрувати поступово.

## Авторизація

`CommandAuthorization` перевіряє три рівні доступу:

- `PUBLIC` — будь-який учасник;
- `CHAT_ADMIN` — адміністратор поточного Telegram-чату або developer;
- `DEVELOPER` — лише ID з `BOT_DEV_IDS`.

Це усуває старе змішування адміністраторів групи з глобальними адміністраторами бота.

## Подальше розширення

Каркас дозволяє без зміни домену додати:

- HTTP/React або Thymeleaf адмін-панель як ще один presentation adapter;
- PostgreSQL замість SQLite;
- автоматичний імпорт каталогу з контрольованим approval-процесом;
- аудит змін цін та ролі операторів;
- шаблони повідомлень для кожної спеціальності.
