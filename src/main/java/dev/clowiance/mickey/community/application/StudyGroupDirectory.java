package dev.clowiance.mickey.community.application;

import dev.clowiance.mickey.community.domain.StudyGroup;

import java.util.List;
import java.util.Optional;

public interface StudyGroupDirectory {
    List<StudyGroup> findActive();
    List<StudyGroup> findAll();
    Optional<StudyGroup> findById(Long id);
    Optional<StudyGroup> findByInviteLink(String inviteLink);
    StudyGroup save(StudyGroup group);
    StudyGroup setActive(Long id, boolean active);
}
