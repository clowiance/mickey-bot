package dev.clowiance.mickey.catalog.bootstrap;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import dev.clowiance.mickey.catalog.domain.TuitionPeriod;

import java.util.List;

public final class UkdCatalogData {

    public static final String BACHELOR_SOURCE = "https://ukd.edu.ua/bakalavrat";
    public static final String MASTER_SOURCE = "https://ukd.edu.ua/mahistratura";

    private UkdCatalogData() {
    }

    public static List<Specialty> entries() {
        return List.of(
                bachelor("b-g17-architecture", "G17", "Архітектура та містобудування",
                        "Архітектура та містобудування", 50_600L, null, null, 1),
                bachelor("b-g19-civil-engineering", "G19", "Будівництво та цивільна інженерія",
                        "Будівництво та цивільна інженерія", 49_600L, 37_500L, null, 2),
                bachelor("b-j2-hospitality", "J2", "Готельно-ресторанна справа та кейтеринг",
                        "Готельно-ресторанна справа", 50_600L, 37_500L, null, 3),
                bachelor("b-b2-design", "B2", "Дизайн", "Графічний дизайн",
                        51_600L, 39_300L, null, 4),
                bachelor("b-c7-journalism", "C7", "Журналістика",
                        "Журналістика та цифрові комунікації", 50_600L, 39_300L,
                        "У таблиці вартості УКД програма зазначена як «Реклама та зв'язки з громадськістю».", 5),
                bachelor("b-f2-software-engineering", "F2", "Інженерія програмного забезпечення",
                        "Розробка та тестування програмного забезпечення", 49_600L, 39_300L, null, 6),
                bachelor("b-d3-project-management", "D3", "Менеджмент",
                        "Project Менеджмент", 50_600L, 39_300L, null, 7),
                bachelor("b-b5-music", "B5", "Музичне мистецтво", "Естрадний спів",
                        50_600L, 39_300L, null, 8),
                bachelor("b-d1-accounting", "D1", "Облік і оподаткування",
                        "Облік та бізнес-аналітика", 49_600L, 37_500L, null, 9),
                bachelor("b-d3-business", "D3", "Менеджмент",
                        "Бізнес та приватне підприємництво", 50_600L, 39_300L, null, 10),
                bachelor("b-d3-logistics", "D3", "Менеджмент", "Логістика",
                        50_600L, 39_300L, "Програма наведена в офіційній таблиці вартості УКД на 2026 рік.", 11),
                bachelor("b-d8-law", "D8", "Право", "Право",
                        50_600L, 39_300L, null, 12),
                bachelor("b-c4-psychology", "C4", "Психологія", "Психологія",
                        51_600L, 39_300L, null, 13),
                bachelor("b-j3-tourism", "J3", "Туризм та рекреація", "Туризм і рекреація",
                        49_600L, 37_500L, null, 14),
                bachelor("b-b11-philology", "B11", "Філологія",
                        "Англійська мова в діловій комунікації (переклад включно)",
                        51_600L, 39_300L, null, 15),
                bachelor("b-d2-finance", "D2", "Фінанси, банківська справа, страхування та фондовий ринок",
                        "Фінанси, банківська справа, страхування та фондовий ринок",
                        49_600L, 37_500L, null, 16),
                bachelor("b-d5-marketing", "D5", "Маркетинг", "Диджитал-маркетинг",
                        50_600L, 39_300L, null, 17),
                bachelor("b-k9-law-enforcement", "K9", "Правоохоронна діяльність",
                        "Правоохоронна діяльність", 51_600L, 39_300L, null, 18),
                bachelor("b-i5-nursing", "I5", "Медсестринство", "Косметологія",
                        49_600L, null, null, 19),
                bachelor("b-i7-therapy", "I7", "Терапія та реабілітація", "Фізична терапія",
                        51_600L, null, null, 20),
                bachelor("b-041-theology", "041", "Богослов'я", "Публічне богослов'я",
                        null, null, "Вартість на офіційній сторінці бакалаврату 2026 не опублікована.", 21),

                master("m-g17-architecture", "G17", "Архітектура та містобудування",
                        "Архітектура та містобудування", 82_500L, null, null, 1),
                master("m-g19-civil-engineering", "G19", "Будівництво та цивільна інженерія",
                        "Будівництво та цивільна інженерія", 82_500L, 73_500L, null, 2),
                master("m-b2-design", "B2", "Дизайн", "Графічний дизайн",
                        82_500L, 73_500L, null, 3),
                master("m-f2-software-engineering", "F2", "Інженерія програмного забезпечення",
                        "Інженерія програмного забезпечення", 82_500L, 73_500L, null, 4),
                master("m-d3-management", "D3", "Менеджмент", "Диджиталізація підприємництва",
                        82_500L, 70_500L, null, 5),
                master("m-d8-law", "D8", "Право", "Право",
                        82_500L, 73_500L, null, 6),
                master("m-d4-public-administration", "D4", "Публічне управління та адміністрування",
                        "Публічне управління та адміністрування", 90_000L, 73_500L, null, 7),
                master("m-b11-philology", "B11", "Філологія",
                        "Англійська мова в міжнародній комунікації", 82_500L, 73_500L, null, 8),
                master("m-c4-psychology", "C4", "Психологія", "Практична психологія",
                        82_500L, 70_500L, null, 9),
                master("m-k9-law-enforcement", "K9", "Правоохоронна діяльність",
                        "Правоохоронна діяльність", 90_000L, 73_500L, null, 10),
                specialty("m-i1-dentistry", "I1", "Стоматологія", "Стоматологія",
                        EducationLevel.MASTER, 51_600L, null, TuitionPeriod.FIRST_YEAR,
                        "383 800 грн за весь період навчання (5 років).", MASTER_SOURCE, 11),
                master("m-i2-medicine", "I2", "Медицина", "Медицина",
                        null, null, "Вартість на офіційній сторінці магістратури 2026 не опублікована.", 12)
        );
    }

    private static Specialty bachelor(String key, String code, String name, String program,
                                      Long fullTime, Long partTime, String note, int order) {
        return specialty(key, code, name, program, EducationLevel.BACHELOR, fullTime, partTime,
                TuitionPeriod.FIRST_YEAR, note, BACHELOR_SOURCE, order);
    }

    private static Specialty master(String key, String code, String name, String program,
                                    Long fullTime, Long partTime, String note, int order) {
        return specialty(key, code, name, program, EducationLevel.MASTER, fullTime, partTime,
                TuitionPeriod.EIGHTEEN_MONTH_PROGRAM, note, MASTER_SOURCE, order);
    }

    private static Specialty specialty(String key, String code, String name, String program,
                                       EducationLevel level, Long fullTime, Long partTime,
                                       TuitionPeriod period, String note, String source, int order) {
        return new Specialty(null, key, code, name, program, level, fullTime, partTime,
                period, note, source, 2026, true, order);
    }
}
