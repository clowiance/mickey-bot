package dev.clowiance.mickey.command;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Будує текст /help під конкретного глядача. До цього довідка була одним статичним рядком:
 * звичайний учасник бачив у ній команди модерації та розробника, які йому все одно
 * відмовляли в доступі. Тепер список збігається з тим, що людина реально може виконати.
 */
@Component
public class CommandHelpRenderer {

    /**
     * Команди беруться ліниво: {@code HelpCommand} сам є {@link ICommand} і залежить від
     * цього рендерера, тож пряме впорскування списку замикало б цикл бінів.
     */
    private final Supplier<List<ICommand>> commands;

    @Autowired
    public CommandHelpRenderer(ObjectProvider<ICommand> commandProvider) {
        this.commands = () -> commandProvider.stream().toList();
    }

    CommandHelpRenderer(List<ICommand> commands) {
        List<ICommand> copy = List.copyOf(commands);
        this.commands = () -> copy;
    }

    /**
     * @param inPrivate чи це діалог із ботом — там доступні лише інформаційні команди
     */
    public String render(boolean chatAdmin, boolean developer, boolean inPrivate) {
        Map<CommandCatalog.Section, Map<String, String>> sections = new EnumMap<>(CommandCatalog.Section.class);

        for (ICommand command : commands.get()) {
            if (!isVisible(command, chatAdmin, developer, inPrivate)) {
                continue;
            }
            CommandCatalog.find(command.getCommand()).ifPresent(entry -> sections
                    .computeIfAbsent(entry.section(), key -> new TreeMap<>())
                    .put(command.getCommand(), "• " + entry.usage() + " — " + entry.description()));
        }

        StringBuilder text = new StringBuilder("⚙️ <b>ДОСТУПНІ ТОБІ КОМАНДИ</b>\n");
        for (CommandCatalog.Section section : CommandCatalog.Section.values()) {
            Map<String, String> lines = sections.get(section);
            if (lines == null || lines.isEmpty()) {
                continue;
            }
            text.append("\n").append(section.title()).append("\n");
            lines.values().forEach(line -> text.append(line).append("\n"));
        }

        if (inPrivate && !developer) {
            text.append("\nℹ️ Команди модерації та статистики працюють у групових чатах.");
        }
        return text.toString();
    }

    private boolean isVisible(ICommand command, boolean chatAdmin, boolean developer, boolean inPrivate) {
        if (inPrivate && command.isGroupOnly()) {
            return false;
        }
        CommandAccess access = command.access();
        if (inPrivate && !developer) {
            // processPrivate пропускає в ЛС лише публічні команди, тому решту не обіцяємо.
            return access == CommandAccess.PUBLIC_INFO;
        }
        return switch (access) {
            case PUBLIC_INFO, GROUP_MEMBER -> true;
            case GROUP_ADMIN -> chatAdmin || developer;
            case DEVELOPER -> developer;
        };
    }
}
