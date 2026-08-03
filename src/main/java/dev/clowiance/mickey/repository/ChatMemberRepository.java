package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByUsernameIgnoreCase(String username);
}
