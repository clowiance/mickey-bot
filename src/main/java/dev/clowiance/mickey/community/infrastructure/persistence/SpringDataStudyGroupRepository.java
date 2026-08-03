package dev.clowiance.mickey.community.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataStudyGroupRepository extends JpaRepository<StudyGroupJpaEntity, Long> {
    List<StudyGroupJpaEntity> findByActiveTrueOrderByCategoryAscSpecialtyAsc();
    List<StudyGroupJpaEntity> findAllByOrderByCategoryAscSpecialtyAsc();
    Optional<StudyGroupJpaEntity> findByInviteLink(String inviteLink);
}
