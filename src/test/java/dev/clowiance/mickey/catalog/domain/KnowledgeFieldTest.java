package dev.clowiance.mickey.catalog.domain;

import dev.clowiance.mickey.catalog.bootstrap.UkdCatalogData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeFieldTest {

    @Test
    void mapsSpecialtyCodesToTheirFields() {
        assertThat(KnowledgeField.fromCode("F2")).isEqualTo(KnowledgeField.IT);
        assertThat(KnowledgeField.fromCode("G19")).isEqualTo(KnowledgeField.ENGINEERING);
        assertThat(KnowledgeField.fromCode("D3")).isEqualTo(KnowledgeField.BUSINESS);
        assertThat(KnowledgeField.fromCode("I1")).isEqualTo(KnowledgeField.HEALTH);
        assertThat(KnowledgeField.fromCode("B11")).isEqualTo(KnowledgeField.CULTURE);
    }

    @Test
    void fallsBackToOtherForUnknownOrLegacyCodes() {
        assertThat(KnowledgeField.fromCode("041")).isEqualTo(KnowledgeField.OTHER);
        assertThat(KnowledgeField.fromCode(null)).isEqualTo(KnowledgeField.OTHER);
        assertThat(KnowledgeField.fromCode("")).isEqualTo(KnowledgeField.OTHER);
    }

    @Test
    void keepsItFirstSoSoughtAfterProgramsAreOnTop() {
        assertThat(KnowledgeField.IT.ordinal()).isZero();
    }

    @Test
    void leavesNoCatalogEntryWithoutMeaningfulGroup() {
        long ungrouped = UkdCatalogData.entries().stream()
                .filter(entry -> KnowledgeField.fromCode(entry.code()) == KnowledgeField.OTHER)
                .count();

        // Єдина позиція без нової літери коду — Богослов'я зі старим кодом 041.
        assertThat(ungrouped).isEqualTo(1);
    }
}
