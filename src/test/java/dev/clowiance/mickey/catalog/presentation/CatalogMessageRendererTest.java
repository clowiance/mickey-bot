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

    @Test
    void rendersDentistryAsFirstYearInsteadOfGenericMasterPeriod() {
        UniversityCatalogService service = mock(UniversityCatalogService.class);
        Specialty dentistry = UkdCatalogData.entries().stream()
                .filter(entry -> entry.catalogKey().equals("m-i1-dentistry"))
                .findFirst()
                .orElseThrow();
        when(service.findActive(EducationLevel.MASTER)).thenReturn(List.of(dentistry));

        String message = new CatalogMessageRenderer(service).renderPrices(EducationLevel.MASTER);

        assertThat(message).contains("І курс — Денна:")
                .contains("383 800 грн")
                .contains("офіційний сайт УКД");
    }
}
