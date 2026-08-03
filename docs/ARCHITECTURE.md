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
Кожен handler декларує namespaces; dispatcher зупиняє старт при перетині callback-префіксів.

## Навчальні групи

```text
community/domain StudyGroup
        ↓
community/application StudyGroupDirectory
        ↓
community/infrastructure/persistence JpaStudyGroupDirectory
        ↓
Telegram /groups, /groupadmin + web admin
```

Hardcoded-список використовується лише як початковий seed. Runtime читає активні записи з SQLite.

## Аналітика Java + Python

```text
analytics/domain
  ChartDefinition, ChartPoint, RenderedChart
        ↓
analytics/application
  AnalyticsDashboardService → ChartRendererPort
        ↓
analytics/infrastructure/python
  PythonChartRenderer → render_chart.py → PNG
        ↓
analytics/presentation
  AnalyticsTelegramService → Telegram SendPhoto
```

Python отримує лише підготовлені агрегати, не має доступу до Telegram-токена чи SQLite.
Скрипт використовує стандартну бібліотеку Python, тому окремий `pip install` не потрібен.
Java-адаптер запускається у bounded executor, обмежує чергу та кешує PNG на одну хвилину.

## Авторизація

`CommandManager` перевіряє типізований `CommandAccess`:

- `PUBLIC` — будь-який учасник;
- `CHAT_ADMIN` — адміністратор поточного Telegram-чату або developer;
- `DEVELOPER` — лише ID з `BOT_DEV_IDS`.

Старі boolean-ознаки залишені як compatibility adapter, доки всі команди не перейдуть на прямий `access()`.

## Web administration та persistence

Thymeleaf-панель `/admin` є окремим presentation adapter над тими самими application-сервісами.
Spring Security надає form login/HTTP Basic і CSRF. Панель за замовчуванням вимкнена та прив'язана до localhost.

Схема створюється й оновлюється Liquibase. Hibernate працює в режимі `validate`. Для SQLite увімкнено
`busy_timeout`, WAL та відкладений старт scheduler-ів; production backup виконується через SQLite `.backup`.

## Подальше розширення

Каркас дозволяє без зміни домену додати:

- React-клієнт або зовнішній API поверх наявного Thymeleaf adapter;
- PostgreSQL замість SQLite;
- автоматичний імпорт каталогу з контрольованим approval-процесом;
- аудит змін цін та ролі операторів;
- шаблони повідомлень для кожної спеціальності.
