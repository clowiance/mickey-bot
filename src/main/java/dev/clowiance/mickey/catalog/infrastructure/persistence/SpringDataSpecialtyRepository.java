package dev.clowiance.mickey.catalog.infrastructure.persistence;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataSpecialtyRepository extends JpaRepository<SpecialtyJpaEntity, Long> {
    List<SpecialtyJpaEntity> findByActiveTrueAndEducationLevelOrderByDisplayOrderAsc(EducationLevel level);

    List<SpecialtyJpaEntity> findByEducationLevelOrderByDisplayOrderAsc(EducationLevel level);

    List<SpecialtyJpaEntity> findAllByOrderByEducationLevelAscDisplayOrderAsc();

    Optional<SpecialtyJpaEntity> findByCatalogKey(String catalogKey);

    long countByActiveTrue();
}
