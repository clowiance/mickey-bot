package dev.clowiance.mickey.catalog.infrastructure.persistence;

import dev.clowiance.mickey.catalog.application.SpecialtyCatalogPort;
import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaSpecialtyCatalogAdapter implements SpecialtyCatalogPort {

    private final SpringDataSpecialtyRepository repository;

    @Override
    public List<Specialty> findActiveByLevel(EducationLevel level) {
        return repository.findByActiveTrueAndEducationLevelOrderByDisplayOrderAsc(level).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Specialty> findAllByLevel(EducationLevel level) {
        return repository.findByEducationLevelOrderByDisplayOrderAsc(level).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Specialty> findAll() {
        return repository.findAllByOrderByEducationLevelAscDisplayOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Specialty> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Specialty> findByCatalogKey(String catalogKey) {
        return repository.findByCatalogKey(catalogKey).map(this::toDomain);
    }

    @Override
    public Specialty save(Specialty specialty) {
        return toDomain(repository.save(toEntity(specialty)));
    }

    @Override
    public long countActive() {
        return repository.countByActiveTrue();
    }

    private Specialty toDomain(SpecialtyJpaEntity entity) {
        return new Specialty(entity.getId(), entity.getCatalogKey(), entity.getCode(), entity.getName(),
                entity.getEducationalProgram(), entity.getEducationLevel(), entity.getFullTimeTuition(),
                entity.getPartTimeTuition(), entity.getTuitionPeriod(), entity.getTuitionNote(),
                entity.getSourceUrl(), entity.getSourceYear(), Boolean.TRUE.equals(entity.getActive()),
                entity.getDisplayOrder());
    }

    private SpecialtyJpaEntity toEntity(Specialty specialty) {
        return SpecialtyJpaEntity.builder()
                .id(specialty.id())
                .catalogKey(specialty.catalogKey())
                .code(specialty.code())
                .name(specialty.name())
                .educationalProgram(specialty.educationalProgram())
                .educationLevel(specialty.educationLevel())
                .fullTimeTuition(specialty.fullTimeTuition())
                .partTimeTuition(specialty.partTimeTuition())
                .tuitionPeriod(specialty.tuitionPeriod())
                .tuitionNote(specialty.tuitionNote())
                .sourceUrl(specialty.sourceUrl())
                .sourceYear(specialty.sourceYear())
                .active(specialty.active())
                .displayOrder(specialty.displayOrder())
                .build();
    }
}
