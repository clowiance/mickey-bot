package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.PollRecord;
import dev.clowiance.mickey.repository.PollRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRecordRepository repository;

    public boolean createPoll(TelegramLongPollingBot bot, Long chatId, String question, List<String> options) {
        try {
            SendPoll sendPoll = new SendPoll();
            sendPoll.setChatId(chatId);
            sendPoll.setQuestion("📊 " + question);
            sendPoll.setOptions(options);
            sendPoll.setIsAnonymous(true);
            sendPoll.setAllowMultipleAnswers(false);
            var message = bot.execute(sendPoll);
            Poll poll = message.getPoll();
            if (poll != null) {
                PollRecord record = PollRecord.builder()
                        .pollId(poll.getId())
                        .chatId(chatId)
                        .question(question)
                        .createdAt(LocalDateTime.now())
                        .closed(false)
                        .build();
                repository.save(record);
            }
            return true;
        } catch (TelegramApiException e) {
            log.error("сьогодні без опитувань", e);
            return false;
        }
    }
}
