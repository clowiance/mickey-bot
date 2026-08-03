package dev.clowiance.mickey.community.infrastructure.persistence;

import dev.clowiance.mickey.community.application.StudyGroupDirectory;
import dev.clowiance.mickey.community.domain.StudyGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaStudyGroupDirectory implements StudyGroupDirectory {
    private final SpringDataStudyGroupRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroup> findActive() {
        return repository.findByActiveTrueOrderByCategoryAscSpecialtyAsc().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroup> findAll() {
        return repository.findAllByOrderByCategoryAscSpecialtyAsc().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudyGroup> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudyGroup> findByInviteLink(String inviteLink) {
        return repository.findByInviteLink(inviteLink).map(this::toDomain);
    }

    @Override
    @Transactional
    public StudyGroup save(StudyGroup group) {
        StudyGroupJpaEntity entity = group.id() == null
                ? new StudyGroupJpaEntity()
                : repository.findById(group.id()).orElseThrow(() -> new IllegalArgumentException("Групу не знайдено"));
        entity.setCategory(group.category());
        entity.setSpecialty(group.specialty());
        entity.setCourse(group.course());
        entity.setInviteLink(group.inviteLink());
        entity.setNote(group.note());
        entity.setActive(group.active());
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public StudyGroup setActive(Long id, boolean active) {
        StudyGroupJpaEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Групу не знайдено: " + id));
        entity.setActive(active);
        return toDomain(repository.save(entity));
    }

    private StudyGroup toDomain(StudyGroupJpaEntity entity) {
        return new StudyGroup(entity.getId(), entity.getCategory(), entity.getSpecialty(), entity.getCourse(),
                entity.getInviteLink(), entity.getNote(), entity.isActive());
    }
}
