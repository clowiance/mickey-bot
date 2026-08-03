package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Optional<ChatMember> findByUsernameIgnoreCase(String username);
=======
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
}
