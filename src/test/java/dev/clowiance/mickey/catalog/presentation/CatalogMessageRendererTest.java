package dev.clowiance.mickey.catalog.presentation;

import dev.clowiance.mickey.catalog.application.UniversityCatalogService;
import dev.clowiance.mickey.catalog.bootstrap.UkdCatalogData;
import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogMessageRendererTest {

    private static final int TELEGRAM_MESSAGE_LIMIT = 4096;

    @Test
    void rendersDentistryWithFirstYearPriceAndFullPeriodNote() {
        Specialty dentistry = entry("m-i1-dentistry");
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.MASTER, List.of(dentistry));

        String message = normalize(renderer.renderPrices(EducationLevel.MASTER));

        assertThat(message).contains("Ціна за І курс")
                .contains("💵 Денна: 51 600 грн")
                .contains("383 800 грн")
                .contains("Офіційний сайт УКД");
    }

    @Test
    void marksMissingPartTimeAsNotOffered() {
        Specialty architecture = entry("b-g17-architecture");
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, List.of(architecture));

        String message = normalize(renderer.renderPrices(EducationLevel.BACHELOR));

        assertThat(message).contains("📘 Заочна: не передбачена");
    }

    @Test
    void showsFullTimePriceRangeInHeader() {
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, bachelors());

        String message = normalize(renderer.renderPrices(EducationLevel.BACHELOR));

        assertThat(message).contains("Денна форма: 49 600 грн – 51 600 грн");
    }

    @Test
    void listsEveryActiveSpecialtyOfLevel() {
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, bachelors());

        String message = normalize(renderer.renderSpecialties(EducationLevel.BACHELOR));

        assertThat(message).contains("21 програма");
        for (Specialty specialty : bachelors()) {
            assertThat(message).contains(specialty.name());
        }
    }

    @Test
    void distinguishesProgramsThatShareSpecialtyCode() {
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, bachelors());

        String message = normalize(renderer.renderPrices(EducationLevel.BACHELOR));

        assertThat(message).contains("Project Менеджмент")
                .contains("Бізнес та приватне підприємництво")
                .contains("Логістика");
    }

    @Test
    void skipsProgramNameThatOnlyRepeatsSpecialtyName() {
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, bachelors());

        String message = normalize(renderer.renderSpecialties(EducationLevel.BACHELOR));

        assertThat(message).doesNotContain("↳ <i>Туризм і рекреація</i>")
                .doesNotContain("↳ <i>Готельно-ресторанна справа</i>")
                .contains("↳ <i>Графічний дизайн</i>");
    }

    @Test
    void groupsSpecialtiesByKnowledgeFieldWithItFirst() {
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, bachelors());

        String message = normalize(renderer.renderSpecialties(EducationLevel.BACHELOR));

        assertThat(message).contains("ІНФОРМАЦІЙНІ ТЕХНОЛОГІЇ")
                .contains("БІЗНЕС, УПРАВЛІННЯ ТА ПРАВО")
                .contains("ОХОРОНА ЗДОРОВ'Я");
        assertThat(message.indexOf("ІНФОРМАЦІЙНІ ТЕХНОЛОГІЇ"))
                .as("ІТ має стояти першою групою")
                .isLessThan(message.indexOf("БІЗНЕС, УПРАВЛІННЯ ТА ПРАВО"));
        assertThat(message.indexOf("Інженерія програмного забезпечення"))
                .as("ІПЗ має бути всередині групи ІТ")
                .isGreaterThan(message.indexOf("ІНФОРМАЦІЙНІ ТЕХНОЛОГІЇ"));
    }

    @Test
    void sortsSpecialtiesAlphabeticallyInsideGroup() {
        CatalogMessageRenderer renderer = rendererFor(EducationLevel.BACHELOR, bachelors());

        String message = normalize(renderer.renderSpecialties(EducationLevel.BACHELOR));

        assertThat(message.indexOf("Маркетинг")).isLessThan(message.indexOf("Облік і оподаткування"));
        assertThat(message.indexOf("Облік і оподаткування")).isLessThan(message.indexOf("Право</b>"));
    }

    @Test
    void keepsEveryCatalogViewWithinTelegramMessageLimit() {
        for (EducationLevel level : EducationLevel.values()) {
            List<Specialty> specialties = UkdCatalogData.entries().stream()
                    .filter(entry -> entry.educationLevel() == level)
                    .toList();
            CatalogMessageRenderer renderer = rendererFor(level, specialties);

            assertThat(renderer.renderPrices(level).length())
                    .as("вартість навчання, %s", level)
                    .isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
            assertThat(renderer.renderSpecialties(level).length())
                    .as("спеціальності, %s", level)
                    .isLessThanOrEqualTo(TELEGRAM_MESSAGE_LIMIT);
        }
    }

    /** Формат uk-UA розділяє тисячі нерозривним пробілом — зводимо до звичайного для порівнянь. */
    private String normalize(String text) {
        return text.replace("\u00A0", " ").replace("\u202F", " ");
    }

    private CatalogMessageRenderer rendererFor(EducationLevel level, List<Specialty> specialties) {
        UniversityCatalogService service = mock(UniversityCatalogService.class);
        when(service.findActive(level)).thenReturn(specialties);
        return new CatalogMessageRenderer(service);
    }

    private List<Specialty> bachelors() {
        return UkdCatalogData.entries().stream()
                .filter(entry -> entry.educationLevel() == EducationLevel.BACHELOR)
                .toList();
    }

    private Specialty entry(String catalogKey) {
        return UkdCatalogData.entries().stream()
                .filter(item -> item.catalogKey().equals(catalogKey))
                .findFirst()
                .orElseThrow();
    }
}
