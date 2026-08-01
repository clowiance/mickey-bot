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
        for (var specialty : UkdCatalogData.entries()) {
            if (catalogPort.findByCatalogKey(specialty.catalogKey()).isEmpty()) {
                catalogPort.save(specialty);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Додано {} позицій до каталогу спеціальностей УКД", inserted);
        }
    }
}
