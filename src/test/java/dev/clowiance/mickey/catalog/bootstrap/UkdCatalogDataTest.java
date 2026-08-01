package dev.clowiance.mickey.catalog.bootstrap;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UkdCatalogDataTest {

    @Test
    void containsComplete2026CatalogWithUniqueKeys() {
        var entries = UkdCatalogData.entries();
        Set<String> uniqueKeys = entries.stream().map(Specialty::catalogKey).collect(Collectors.toSet());

        assertThat(entries).hasSize(33);
        assertThat(uniqueKeys).hasSize(entries.size());
        assertThat(entries).allMatch(entry -> entry.sourceYear() == 2026);
        assertThat(entries).filteredOn(entry -> entry.educationLevel() == EducationLevel.BACHELOR).hasSize(21);
        assertThat(entries).filteredOn(entry -> entry.educationLevel() == EducationLevel.MASTER).hasSize(12);
    }

    @Test
    void preservesOfficialKeyPricesAndMissingValues() {
        Specialty bachelorSoftware = byKey("b-f2-software-engineering");
        Specialty dentistry = byKey("m-i1-dentistry");
        Specialty medicine = byKey("m-i2-medicine");

        assertThat(bachelorSoftware.fullTimeTuition()).isEqualTo(49_600L);
        assertThat(bachelorSoftware.partTimeTuition()).isEqualTo(39_300L);
        assertThat(dentistry.fullTimeTuition()).isEqualTo(51_600L);
        assertThat(dentistry.tuitionNote()).contains("383 800").contains("5 років");
        assertThat(medicine.fullTimeTuition()).isNull();
        assertThat(medicine.tuitionNote()).contains("не опублікована");
    }

    private Specialty byKey(String key) {
        return UkdCatalogData.entries().stream()
                .filter(entry -> entry.catalogKey().equals(key))
                .findFirst()
                .orElseThrow();
    }
}
