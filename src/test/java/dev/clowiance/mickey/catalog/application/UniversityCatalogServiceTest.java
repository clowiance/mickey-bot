package dev.clowiance.mickey.catalog.application;

import dev.clowiance.mickey.catalog.bootstrap.UkdCatalogData;
import dev.clowiance.mickey.catalog.domain.Specialty;
import dev.clowiance.mickey.catalog.domain.StudyForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityCatalogServiceTest {

    @Mock
    private SpecialtyCatalogPort catalogPort;

    private UniversityCatalogService service;

    @BeforeEach
    void setUp() {
        service = new UniversityCatalogService(catalogPort);
        when(catalogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updatesOnlySelectedStudyForm() {
        Specialty original = UkdCatalogData.entries().stream()
                .filter(entry -> entry.catalogKey().equals("b-f2-software-engineering"))
                .findFirst()
                .orElseThrow();
        when(catalogPort.findById(10L)).thenReturn(Optional.of(original));

        Specialty updated = service.updateTuition(10L, StudyForm.PART_TIME, 40_000L);

        assertThat(updated.fullTimeTuition()).isEqualTo(49_600L);
        assertThat(updated.partTimeTuition()).isEqualTo(40_000L);
    }
}
