package com.dumptruckman.minecraft.pluginbase.bukkit;

import com.dumptruckman.minecraft.pluginbase.command.CommandHandler;
import com.dumptruckman.minecraft.pluginbase.command.CommandRegistration;
import com.dumptruckman.minecraft.pluginbase.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

class BukkitCommandHandler extends CommandHandler<BukkitPlugin> {

    private final CommandExecutor executor;
    private CommandMap fallbackCommands;

    public BukkitCommandHandler(BukkitPlugin plugin) {
        this(plugin, plugin);
    }

    public BukkitCommandHandler(BukkitPlugin plugin, CommandExecutor executor) {
        super(plugin);
        this.executor = executor;
    }

    protected boolean register(@NotNull final CommandRegistration commandInfo) {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            return false;
        }
        DynamicPluginCommand cmd = new DynamicPluginCommand(commandInfo.getAliases(), commandInfo.getDesc(),
                "/" + commandInfo.getName() + " " + commandInfo.getUsage(), executor, commandInfo.getRegisteredWith(), plugin);
        cmd.setPermissions(commandInfo.getPermissions());
        commandMap.register(commandInfo.getName(), plugin.getDescription().getName(), cmd);
        return true;
    }

    private CommandMap getCommandMap() {
        CommandMap commandMap = ReflectionUtil.getField(plugin.getServer().getPluginManager(), "commandMap");
        if (commandMap == null) {
            if (fallbackCommands != null) {
                commandMap = fallbackCommands;
            } else {
                Logging.warning("Could not retrieve server CommandMap, using fallback instead!");
                fallbackCommands = commandMap = new SimpleCommandMap(Bukkit.getServer());
                Bukkit.getServer().getPluginManager().registerEvents(new FallbackRegistrationListener(fallbackCommands), plugin);
            }
        }
        return commandMap;
    }

    boolean hasPermission(final CommandSender sender, final String permission) {
        return plugin.wrapSender(sender).hasPermission(permission);
    }
}
