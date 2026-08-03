# Mickey Bot — бот спільнот УКД

Telegram-бот для абітурієнтських і студентських груп Університету Короля Данила. Працює одночасно з багатьма групами, модеруванням, репортами, опитуваннями, відкладеними розсилками та актуальним каталогом вступу.

## Технології

- Java 21
- Spring Boot 3.3
- Telegram Bots API
- Spring Data JPA + SQLite (WAL, Liquibase)
- Gradle 8.14
- Python 3 — генерація PNG-діаграм без сторонніх пакетів

## Дані УКД

Каталог містить 33 позиції вступної кампанії 2026:

- 21 позиція бакалаврату;
- 12 позицій магістратури;
- денну та заочну вартість там, де її опублікував УКД;
- окрему примітку для стоматології: перший рік і повний п'ятирічний цикл;
- явну позначку «не опубліковано» для програм без офіційної ціни.

Офіційні джерела, перевірені 30.07.2026:

- [Бакалаврат і вартість 2026](https://ukd.edu.ua/bakalavrat)
- [Магістратура і вартість 2026](https://ukd.edu.ua/mahistratura)
- [Спеціальності та освітні програми](https://ukd.edu.ua/specialnosti-ta-osvitni-programi)

Початкові дані синхронізуються за `catalogKey`: офіційні назви, джерела й ціни оновлюються з seed,
а ручний прапорець `active` не перезаписується після рестарту.

## Запуск

```powershell
Copy-Item .env.example .env
code .env
.\gradlew.bat bootRun
```

Обов'язкові змінні:

```env
BOT_USERNAME=bot_username
BOT_TOKEN=token_from_botfather
BOT_DEV_IDS=123456789,987654321
```

Для першого production-запуску рекомендується спочатку створити backup:

```powershell
.\scripts\backup-db.ps1
```

Liquibase автоматично оновить стару SQLite-схему, після чого Hibernate лише перевірить її через
`ddl-auto: validate`. База працює з `busy_timeout` і WAL; scheduler-и стартують після завершення seed/migration.

## Команди користувача

- `/menu` — головне меню
- `/specialties` — актуальні спеціальності бакалаврату й магістратури
- `/prices` — офіційна вартість навчання у 2026 році
- `/faq` — часті запитання
- `/report` — скарга на користувача
- `/groups` — активні навчальні групи зі сховища (команда доступна лише всередині Telegram-груп)

## Керування групою

Адміністратор Telegram-групи відкриває `/adminpanel` і може:

- призначити групі спеціальність;
- перемикати тип аудиторії: абітурієнти, студенти або змішана;
- окремо вмикати привітання, модерацію та розсилки;
- переглядати репорти та статистику групи.
- будувати діаграми групи та активності за 30 днів.

Діаграми також доступні командою:

```text
/charts group
/charts activity
/charts rating
/charts earned
```

Java формує зріз із SQLite, а Python-адаптер із
`src/main/resources/analytics/render_chart.py` генерує PNG. `matplotlib` встановлювати не потрібно.

Кожна група має незалежний запис у таблиці `managed_chat_settings`.

## Панель розробника

Користувачі з `BOT_DEV_IDS` мають `/devpanel` і додаткові команди:

```text
/catalogadmin list bachelor
/catalogadmin list master
/catalogadmin price ID full 50600
/catalogadmin price ID part none
/catalogadmin active ID
/broadcastall all 2026-08-01 12:00 Текст
/broadcastall SPECIALTY_ID 2026-08-01 12:00 Текст
/groupadmin list
/groupadmin add Назва|1 курс|https://t.me/+...|Примітка
/groupadmin disable ID
```

У `/devpanel` доступні глобальна діаграма бота й рейтинг економіки.

Ролі розділені на `PUBLIC`, `CHAT_ADMIN` і `DEVELOPER`. Адміністратор однієї групи не отримує глобальних developer-прав.

## Web-адмінка

За замовчуванням web-панель вимкнена і сервер слухає лише localhost. Для ввімкнення:

```env
ADMIN_PANEL_ENABLED=true
ADMIN_PANEL_USERNAME=admin
ADMIN_PANEL_PASSWORD=replace-with-a-long-random-password
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8080
```

Після запуску відкрий `http://127.0.0.1:8080/admin`. Панель захищена Spring Security, HTTP Basic/form login
і CSRF. У ній можна:

- перемикати welcome, модерацію та розсилки окремого чату;
- додавати й вимикати Telegram-інвайти;
- змінювати ціни та активність спеціальностей;
- переглядати, скасовувати й повторювати невдалі розсилки;
- переглядати health і metrics.

Не виставляй `SERVER_ADDRESS=0.0.0.0` без HTTPS/reverse proxy і firewall.

## Перевірка

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Тести перевіряють каталог УКД, рольову модель, незалежні налаштування груп, HTML-безпеку,
lifecycle розсилок, міграцію старої SQLite-бази та захист/рендер web-адмінки.

Докладніше про модулі: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

Результати технічного аудиту: [docs/AUDIT-2026-08-03.md](docs/AUDIT-2026-08-03.md).
