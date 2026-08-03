package dev.clowiance.mickey.catalog.application;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;

import java.util.List;
import java.util.Optional;

public interface SpecialtyCatalogPort {
    List<Specialty> findActiveByLevel(EducationLevel level);

    List<Specialty> findAllByLevel(EducationLevel level);

    List<Specialty> findAll();

    Optional<Specialty> findById(Long id);

    Optional<Specialty> findByCatalogKey(String catalogKey);

    Specialty save(Specialty specialty);

    long countActive();
}
