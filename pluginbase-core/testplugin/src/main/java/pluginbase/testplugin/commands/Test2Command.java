package pluginbase.testplugin.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.command.Command;
import pluginbase.command.CommandContext;
import pluginbase.command.CommandInfo;
import pluginbase.command.CommandProvider;
import pluginbase.messages.Message;
import pluginbase.minecraft.BasePlayer;
import pluginbase.permission.Perm;
import pluginbase.testplugin.UnregisteredLanguage;

import java.util.Arrays;
import java.util.List;

@CommandInfo(
        primaryAlias = "test2",
        desc = "Just another test command.",
        usage = "<name> [action]"
)
public class Test2Command extends Command {

    public Test2Command(@NotNull CommandProvider commandProvider) {
        super(commandProvider);
    }

    @Nullable
    @Override
    public Perm getPerm() {
        return null;
    }

    @Nullable
    @Override
    public Message getHelp() {
        return null;
    }

    @Override
    public boolean runCommand(@NotNull BasePlayer sender, @NotNull CommandContext context) {
        getMessager().message(sender, UnregisteredLanguage.TEST);
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull BasePlayer sender, @NotNull CommandContext context) {
        return Arrays.asList("crape");
    }
}
