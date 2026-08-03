package dev.clowiance.mickey.community.bootstrap;

import dev.clowiance.mickey.community.domain.StudyGroup;
import dev.clowiance.mickey.community.domain.StudyGroupCategory;

import java.util.List;

/**
 * Перелік чатів навчальних груп УКД. Дані перенесені з таблиці кураторів, посилання —
 * інвайти Telegram, тому правити їх треба саме тут: чат може бути перестворений,
 * і тоді старий інвайт перестане працювати.
 */
public final class UkdStudyGroupData {

    private UkdStudyGroupData() {
    }

    public static List<StudyGroup> entries() {
        return List.of(
                university("Інженерія програмного забезпечення (ІПЗ)", "1 курс",
                        "https://t.me/+GZsXf7J8r8VmM2My"),
                university("Архітектура та містобудування", "1 курс",
                        "https://t.me/+IuGMoGz9tyRmZDBi"),
                university("Готельно-ресторанна справа (ГРС)", "1 курс",
                        "https://t.me/+aIrV8xm-yn85MmE6"),
                university("Графічний дизайн", "1 курс",
                        "https://t.me/+o6vCBIYjcuk3ODYy"),
                university("Логістика", "1 курс",
                        "https://t.me/+JZPdEYC5Q_EyZjdi"),
                university("Маркетинг", "1 курс",
                        "https://t.me/+KURcTTDpCV0zOGQy"),
                university("Право", "1 курс",
                        "https://t.me/+V0a4kWNnk-82MjAy"),
                university("Будівництво та цивільна інженерія (G19)", null,
                        "https://t.me/+TYur5y1rcBIkOTdi")
        );
    }

    private static StudyGroup university(String specialty, String course, String inviteLink) {
        return new StudyGroup(StudyGroupCategory.UNIVERSITY, specialty, course, inviteLink, null);
    }

}
