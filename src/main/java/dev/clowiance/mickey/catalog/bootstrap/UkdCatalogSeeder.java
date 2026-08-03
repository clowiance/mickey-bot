package dev.clowiance.mickey.catalog.bootstrap;

import dev.clowiance.mickey.catalog.application.SpecialtyCatalogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UkdCatalogSeeder implements ApplicationRunner {

    private final SpecialtyCatalogPort catalogPort;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = 0;
        int updated = 0;
        for (var specialty : UkdCatalogData.entries()) {
            var existing = catalogPort.findByCatalogKey(specialty.catalogKey());
            if (existing.isEmpty()) {
                catalogPort.save(specialty);
                inserted++;
            } else if (officialDataChanged(existing.get(), specialty)) {
                var current = existing.get();
                catalogPort.save(new dev.clowiance.mickey.catalog.domain.Specialty(
                        current.id(), specialty.catalogKey(), specialty.code(), specialty.name(),
                        specialty.educationalProgram(), specialty.educationLevel(), specialty.fullTimeTuition(),
                        specialty.partTimeTuition(), specialty.tuitionPeriod(), specialty.tuitionNote(),
                        specialty.sourceUrl(), specialty.sourceYear(), current.active(), specialty.displayOrder()));
                updated++;
            }
        }
        if (inserted > 0 || updated > 0) {
            log.info("Каталог УКД синхронізовано: додано {}, оновлено {}", inserted, updated);
        }
    }

    private boolean officialDataChanged(dev.clowiance.mickey.catalog.domain.Specialty current,
                                        dev.clowiance.mickey.catalog.domain.Specialty seed) {
        return !java.util.Objects.equals(current.code(), seed.code())
                || !java.util.Objects.equals(current.name(), seed.name())
                || !java.util.Objects.equals(current.educationalProgram(), seed.educationalProgram())
                || current.educationLevel() != seed.educationLevel()
                || !java.util.Objects.equals(current.fullTimeTuition(), seed.fullTimeTuition())
                || !java.util.Objects.equals(current.partTimeTuition(), seed.partTimeTuition())
                || current.tuitionPeriod() != seed.tuitionPeriod()
                || !java.util.Objects.equals(current.tuitionNote(), seed.tuitionNote())
                || !java.util.Objects.equals(current.sourceUrl(), seed.sourceUrl())
                || current.sourceYear() != seed.sourceYear()
                || current.displayOrder() != seed.displayOrder();
    }
}
