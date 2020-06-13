package pluginbase.sponge.command;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
import pluginbase.command.Command;
import pluginbase.command.CommandHandler;
import pluginbase.command.CommandProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SpongeCommandHandler extends CommandHandler {

    private final static SimpleDispatcher rootCommand = new SimpleDispatcher();

    public SpongeCommandHandler(@NotNull CommandProvider commandProvider) {
        super(commandProvider);
        Sponge.getCommandManager().register(commandProvider.getPlugin(), rootCommand, commandProvider.getCommandPrefix());
    }

    @Override
    protected boolean requiresPrefix() {
        return true;
    }

    @Override
    protected boolean register(@NotNull CommandRegistration commandInfo, @NotNull Command command) {
        List<String> subCommands;
        for (String alias : commandInfo.getAliases()) {
            subCommands = new ArrayList<>(Arrays.asList(PATTERN_ON_SPACE.split(alias.substring(alias.indexOf(' ') + 1))));
            if (!register(commandInfo, command, rootCommand, subCommands)) return false; // TODO: undo();
        }
        return true;
    }

    private boolean register(@NotNull CommandRegistration commandInfo, @NotNull Command command, SimpleDispatcher d, List<String> subCommands) {
        if (subCommands.size() == 1) {
            SpongeCommand spongeCommand = new SpongeCommand(commandProvider, command, commandInfo.getUsage(), commandInfo.getDesc());
            return d.register(spongeCommand, subCommands).isPresent();
        }

        SimpleDispatcher subCommand = new SimpleDispatcher();
        Optional<CommandMapping> mapping = d.register(subCommand, subCommands.remove(0));
        if (mapping.isPresent()) return register(commandInfo, command, subCommand, subCommands);
        else return false;
    }

    /*
    @NotNull
    @Override
    public List<String> getSuggestions(@NotNull CommandSource commandSource, @NotNull String args) throws CommandException {
        final BasePlayer wrappedSender = SpongeTools.wrapSender(commandSource);
        return tabComplete(wrappedSender, args.split("\\s"));
    }
    */
}
