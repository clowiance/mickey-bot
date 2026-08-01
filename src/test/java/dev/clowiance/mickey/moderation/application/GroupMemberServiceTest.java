package dev.clowiance.mickey.moderation.application;

import dev.clowiance.mickey.moderation.domain.GroupMemberState;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMemberServiceTest {

    @Test
    void keepsWarningsIndependentBetweenGroups() {
        InMemoryStatePort port = new InMemoryStatePort();
        GroupMemberService service = new GroupMemberService(port);

        service.addWarn(100L, 7L, "student", "Student");
        service.addWarn(100L, 7L, "student", "Student");
        service.addWarn(200L, 7L, "student", "Student");

        assertThat(service.find(100L, 7L).warnCount()).isEqualTo(2);
        assertThat(service.find(200L, 7L).warnCount()).isEqualTo(1);
        assertThat(service.statistics(100L).memberCount()).isEqualTo(1);
        assertThat(service.statistics(100L).totalWarnings()).isEqualTo(2);
    }

    private static final class InMemoryStatePort implements GroupMemberStatePort {
        private final Map<String, GroupMemberState> states = new HashMap<>();

        @Override
        public Optional<GroupMemberState> find(Long chatId, Long userId) {
            return Optional.ofNullable(states.get(key(chatId, userId)));
        }

        @Override
        public List<GroupMemberState> findByChatId(Long chatId) {
            return states.values().stream().filter(state -> state.chatId().equals(chatId)).toList();
        }

        @Override
        public GroupMemberState save(GroupMemberState state) {
            states.put(key(state.chatId(), state.userId()), state);
            return state;
        }

        private String key(Long chatId, Long userId) {
            return chatId + ":" + userId;
        }
    }
}
