package dev.clowiance.mickey.telegram;

import dev.clowiance.mickey.command.CommandAccess;
import dev.clowiance.mickey.command.CommandCatalog;
import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.config.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;

/**
 * Наповнює синє меню команд Telegram. Раніше {@code SetMyCommands} не викликався ніде,
 * тому меню біля поля вводу залишалося порожнім, скільки б команд бот не мав.
 *
 * <p>Списки різні за областями видимості: в особистих чатах показуємо лише те, що бот
 * там реально виконує, у групах — загальні команди, адміністраторам — плюс керування,
 * а команди розробника видно тільки в приватних чатах самих розробників.
 */
@Slf4j
@Component
public class TelegramCommandRegistrar {

    /** Telegram обрізає опис довший за 256 символів. */
    private static final int DESCRIPTION_LIMIT = 256;

    private final List<ICommand> commands;
    private final BotProperties botProperties;

    public TelegramCommandRegistrar(List<ICommand> commands, BotProperties botProperties) {
        this.commands = List.copyOf(commands);
        this.botProperties = botProperties;
    }

    public void register(TelegramLongPollingBot bot) {
        apply(bot, new BotCommandScopeDefault(), publicCommands(), "default");
        apply(bot, new BotCommandScopeAllPrivateChats(), publicCommands(), "private");
        apply(bot, new BotCommandScopeAllGroupChats(), groupCommands(), "groups");
        apply(bot, new BotCommandScopeAllChatAdministrators(), adminCommands(), "admins");

        List<Long> devIds = botProperties.getDevIds();
        if (devIds != null) {
            for (Long devId : devIds) {
                apply(bot, BotCommandScopeChat.builder().chatId(String.valueOf(devId)).build(),
                        developerCommands(), "dev:" + devId);
            }
        }
    }

    private List<BotCommand> publicCommands() {
        return toBotCommands(command -> !command.isGroupOnly() && command.access() == CommandAccess.PUBLIC_INFO);
    }

    private List<BotCommand> groupCommands() {
        return toBotCommands(command -> {
            CommandAccess access = command.access();
            return access == CommandAccess.PUBLIC_INFO || access == CommandAccess.GROUP_MEMBER;
        });
    }

    private List<BotCommand> adminCommands() {
        return toBotCommands(command -> command.access() != CommandAccess.DEVELOPER);
    }

    private List<BotCommand> developerCommands() {
        return toBotCommands(command -> !command.isGroupOnly());
    }

    private List<BotCommand> toBotCommands(java.util.function.Predicate<ICommand> filter) {
        return commands.stream()
                .filter(filter)
                .filter(command -> CommandCatalog.find(command.getCommand()).isPresent())
                .sorted(Comparator.comparing(ICommand::getCommand))
                .map(command -> BotCommand.builder()
                        .command(command.getCommand().substring(1))
                        .description(shorten(CommandCatalog.find(command.getCommand()).orElseThrow().description()))
                        .build())
                .toList();
    }

    private String shorten(String description) {
        return description.length() <= DESCRIPTION_LIMIT
                ? description
                : description.substring(0, DESCRIPTION_LIMIT - 1) + "…";
    }

    private void apply(TelegramLongPollingBot bot, BotCommandScope scope, List<BotCommand> botCommands, String label) {
        if (botCommands.isEmpty()) {
            return;
        }
        try {
            bot.execute(SetMyCommands.builder()
                    .commands(botCommands)
                    .scope(scope)
                    .build());
            log.info("Меню команд Telegram оновлено для {}: {} команд", label, botCommands.size());
        } catch (TelegramApiException exception) {
            // Порожнє меню команд не ламає бота — команди все одно приймаються текстом.
            log.warn("Не вдалося оновити меню команд Telegram для {}", label, exception);
        }
    }
}
