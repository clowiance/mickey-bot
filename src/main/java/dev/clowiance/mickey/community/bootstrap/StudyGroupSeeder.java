package dev.clowiance.mickey.community.bootstrap;

import dev.clowiance.mickey.community.application.StudyGroupDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyGroupSeeder implements ApplicationRunner {
    private final StudyGroupDirectory directory;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = 0;
        for (var group : UkdStudyGroupData.entries()) {
            if (directory.findByInviteLink(group.inviteLink()).isEmpty()) {
                directory.save(group);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Додано {} посилань на навчальні групи", inserted);
        }
    }
}
