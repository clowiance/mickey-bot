package dev.clowiance.mickey.repository;

import dev.clowiance.mickey.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
}
