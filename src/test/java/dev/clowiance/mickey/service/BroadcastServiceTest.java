package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.BroadcastStatus;
import dev.clowiance.mickey.entity.ScheduledBroadcast;
import dev.clowiance.mickey.repository.ScheduledBroadcastRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BroadcastServiceTest {
    @Mock
    ScheduledBroadcastRepository repository;
    @InjectMocks
    BroadcastService service;

    @Test
    void rejectsTelegramMessagesThatCannotBeDelivered() {
        assertThatThrownBy(() -> service.schedule(1L, "x".repeat(3901), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3900");
    }

    @Test
    void movesBroadcastToDeadLetterAfterFifthFailure() {
        ScheduledBroadcast broadcast = ScheduledBroadcast.builder()
                .id(7L).chatId(1L).text("news").sendAt(LocalDateTime.now())
                .sent(false).status(BroadcastStatus.PROCESSING).attemptCount(4).build();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markFailed(broadcast, new IllegalStateException("network"));

        assertThat(broadcast.getStatus()).isEqualTo(BroadcastStatus.DEAD_LETTER);
        assertThat(broadcast.getAttemptCount()).isEqualTo(5);
        assertThat(broadcast.getLastError()).isEqualTo("network");
        assertThat(broadcast.getNextAttemptAt()).isNull();
        verify(repository).save(broadcast);
    }
}
