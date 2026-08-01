package dev.clowiance.mickey.catalog.application;

import dev.clowiance.mickey.catalog.domain.EducationLevel;
import dev.clowiance.mickey.catalog.domain.Specialty;
import dev.clowiance.mickey.catalog.domain.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityCatalogService {

    private final SpecialtyCatalogPort catalogPort;

    @Transactional(readOnly = true)
    public List<Specialty> findActive(EducationLevel level) {
        return catalogPort.findActiveByLevel(level);
    }

    @Transactional(readOnly = true)
    public List<Specialty> findAll(EducationLevel level) {
        return catalogPort.findAllByLevel(level);
    }

    @Transactional(readOnly = true)
    public List<Specialty> findAll() {
        return catalogPort.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Specialty> findById(Long id) {
        return catalogPort.findById(id);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return catalogPort.countActive();
    }

    @Transactional
    public Specialty updateTuition(Long specialtyId, StudyForm form, Long amount) {
        if (amount != null && amount < 0) {
            throw new IllegalArgumentException("Tuition cannot be negative");
        }
        Specialty specialty = requireSpecialty(specialtyId);
        return catalogPort.save(specialty.withTuition(form, amount));
    }

    @Transactional
    public Specialty toggleActive(Long specialtyId) {
        Specialty specialty = requireSpecialty(specialtyId);
        return catalogPort.save(specialty.withActive(!specialty.active()));
    }

    private Specialty requireSpecialty(Long specialtyId) {
        return catalogPort.findById(specialtyId)
                .orElseThrow(() -> new IllegalArgumentException("Specialty not found: " + specialtyId));
    }
}
