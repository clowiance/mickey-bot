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
                        "https://t.me/+TYur5y1rcBIkOTdi"),

                college("Інженерія програмного забезпечення (ІПЗ)", null,
                        "https://t.me/+XYVIorkFAU05YzQy", "Одногрупники коледжу"),
                college("Готельно-ресторанна справа", null,
                        "https://t.me/+a7wh3ZLAgMw1ODc6", null),
                college("Дизайн інтер'єру", null,
                        "https://t.me/+QQobfuw47Kw1Zjky", null),
                college("Менеджмент", null,
                        "https://t.me/+fHBPYPS85yk2MDJi", null),
                college("Юридичне діловодство та офісна справа", null,
                        "https://t.me/+b1IE8rbkFk0yZmQ6", null),
                college("Правоохоронна діяльність", null,
                        "https://t.me/+MTJCNXqRJvkwY2Qy", null),
                college("Хореографія", null,
                        "https://t.me/+AP2SgXSx-x83Mjcy", null),
                college("Маркетинг", "2 курс",
                        "https://t.me/+MWOch-CxleRiOTJi", null),
                college("Маркетинг", "Після 9 класу",
                        "https://t.me/+oicN3thi9kVjYTYy", null),
                college("Правоохоронна діяльність", "Після 9 класу",
                        "https://t.me/+VZvbm68iX5M3MDAy", null),
                college("Туризм", "Після 9 класу",
                        "https://t.me/+AnMT_QFMFY4MjQy", null),

                new StudyGroup(StudyGroupCategory.UNSPECIFIED, "Косметологія", null,
                        "https://t.me/+qRsuPZf-lml2MjAy", "Категорію не зазначено")
        );
    }

    private static StudyGroup university(String specialty, String course, String inviteLink) {
        return new StudyGroup(StudyGroupCategory.UNIVERSITY, specialty, course, inviteLink, null);
    }

    private static StudyGroup college(String specialty, String course, String inviteLink, String note) {
        return new StudyGroup(StudyGroupCategory.COLLEGE, specialty, course, inviteLink, note);
    }
}
