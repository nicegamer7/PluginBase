package pluginbase.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcstats.Metrics;
import pluginbase.bukkit.command.BukkitCommandHandler;
import pluginbase.bukkit.command.BukkitCommandProvider;
import pluginbase.bukkit.config.BukkitConfiguration;
import pluginbase.bukkit.config.YamlConfiguration;
import pluginbase.bukkit.messaging.BukkitMessager;
import pluginbase.bukkit.minecraft.BukkitTools;
import pluginbase.bukkit.permission.BukkitPermFactory;
import pluginbase.command.CommandHandler;
import pluginbase.command.CommandProvider;
import pluginbase.jdbc.DatabaseSettings;
import pluginbase.messages.PluginBaseException;
import pluginbase.messages.messaging.Messager;
import pluginbase.messages.messaging.SendablePluginBaseException;
import pluginbase.minecraft.BasePlayer;
import pluginbase.permission.PermFactory;
import pluginbase.plugin.PluginAgent;
import pluginbase.plugin.PluginBase;
import pluginbase.plugin.PluginInfo;
import pluginbase.plugin.ServerInterface;
import pluginbase.plugin.Settings;
import pluginbase.plugin.Settings.Language;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class BukkitPluginAgent<P> extends PluginAgent<P> {

    static {
        PermFactory.registerPermissionFactory(BukkitPermFactory.class);
    }

    @NotNull
    public static <P> BukkitPluginAgent<P> getPluginAgent(@NotNull Class<P> pluginInterface, @NotNull Plugin plugin, @NotNull String commandPrefix) {
        if (!pluginInterface.isInstance(plugin)) {
            throw new IllegalArgumentException("pluginInterface must be a superclass or superinterface of plugin.");
        }
        return new BukkitPluginAgent<P>(pluginInterface, plugin, commandPrefix, true);
    }

    @NotNull
    public static <P> BukkitPluginAgent<P> getPluginAgentNoQueuedCommands(@NotNull Class<P> pluginInterface, @NotNull Plugin plugin, @NotNull String commandPrefix) {
        if (!pluginInterface.isInstance(plugin)) {
            throw new IllegalArgumentException("pluginInterface must be a superclass or superinterface of plugin.");
        }
        return new BukkitPluginAgent<P>(pluginInterface, plugin, commandPrefix, false);
    }

    @NotNull
    private final Plugin plugin;
    private PluginInfo pluginInfo;
    private final ServerInterface serverInterface;
    @Nullable
    private Metrics metrics = null;

    private BukkitConfiguration config;

    private BukkitPluginAgent(@NotNull Class<P> pluginInterface, @NotNull Plugin plugin, @NotNull String commandPrefix, boolean queuedCommands) {
        super(pluginInterface, (P) plugin, queuedCommands ? BukkitCommandProvider.getBukkitCommandProvider(plugin, commandPrefix) : BukkitCommandProvider.getBukkitCommandProviderNoQueuedCommands(plugin, commandPrefix));
        this.plugin = plugin;
        this.serverInterface = new BukkitServerInterface(plugin);
    }

    public void enableMetrics() throws IOException {
        getMetrics().enable();
    }

    public void disableMetrics() throws IOException {
        getMetrics().disable();
    }

    /**
     * Gets the metrics object for the plugin.
     *
     * @return the metrics object for the plugin or null if something went wrong while enabling one or if the
     * server chooses not to use metrics.
     */
    @NotNull
    public Metrics getMetrics() throws IOException {
        if (metrics == null) {
            metrics = new Metrics(plugin);
        }
        return metrics;
    }

    @NotNull
    @Override
    protected PluginInfo getPluginInfo() {
        if (pluginInfo == null) {
            pluginInfo = new BukkitPluginInfo(plugin);
        }
        return pluginInfo;
    }

    @NotNull
    @Override
    protected File getDataFolder() {
        return plugin.getDataFolder();
    }

    @NotNull
    @Override
    protected Settings loadSettings() {
        Settings defaults = getDefaultSettings();
        Settings settings = defaults;
        try {
            config = BukkitConfiguration.loadYamlConfig(getConfigFile());
            ((YamlConfiguration) config).options().comments(true);

            if (config.contains("settings")) {
                settings = config.getToObject("settings", defaults);
                if (settings == null) { // Should never be true
                    settings = defaults;
                }
            } else {
                settings = defaults;
            }
            config.set("settings", settings);
            config.save(getConfigFile());
            getLog().fine("Loaded config file!");
        } catch (PluginBaseException e) {  // Catch errors loading the config file and exit out if found.
            getLog().severe("Error loading config file!");
            e.logException(getLog(), Level.SEVERE);
            disablePlugin();
        } catch (IOException e) {
            getLog().severe("There was a problem saving the config file!");
            e.printStackTrace();
        }
        getLog().setDebugLevel(getLog().getDebugLevel());
        return settings;
    }

    @NotNull
    @Override
    public DatabaseSettings loadDatabaseSettings(@NotNull DatabaseSettings defaults) {
        DatabaseSettings settings = defaults;
        try {
            BukkitConfiguration sqlConfig = BukkitConfiguration.loadYamlConfig(getSqlConfigFile());
            ((YamlConfiguration) sqlConfig).options().comments(true);
            if (sqlConfig.contains("settings")) {
                settings = sqlConfig.getToObject("settings", defaults);
            }
            sqlConfig.set("settings", settings);
            sqlConfig.save(getSqlConfigFile());
            getLog().fine("Loaded db config file!");
        } catch (PluginBaseException e) {
            getLog().severe("Could not create db_config.yml!");
            e.logException(getLog(), Level.SEVERE);
        } catch (IOException e) {
            getLog().severe("There was a problem saving the db config file!");
            e.printStackTrace();
        }
        return settings;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettings() throws SendablePluginBaseException {
        config.set("settings", getPluginBase().getSettings());
        try {
            config.save(getConfigFile());
        } catch (IOException e) {
            new PluginBaseException(e).logException(getLog(), Level.WARNING);
        }
    }

    @Override
    protected void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    @Override
    protected ServerInterface getServerInterface() {
        return serverInterface;
    }

    public boolean callCommand(CommandSender sender, org.bukkit.command.Command command, String commandLabel, String[] args) {
        if (!plugin.isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        final BasePlayer wrappedSender = BukkitTools.wrapSender(sender);
        return callCommand(wrappedSender, command.getName(), args);
    }
}
