package com.dumptruckman.minecraft.test.utils;

import com.dumptruckman.minecraft.config.AbstractYamlConfig;
import com.dumptruckman.minecraft.config.BaseConfig;
import com.dumptruckman.minecraft.config.ConfigEntry;
import com.dumptruckman.minecraft.config.Null;
import com.dumptruckman.minecraft.config.SimpleConfigEntry;
import com.dumptruckman.minecraft.plugin.BukkitPlugin;
import com.dumptruckman.minecraft.plugin.PluginBase;

import java.io.IOException;

public class MockConfig extends AbstractYamlConfig implements BaseConfig {
    
    public static final ConfigEntry<Boolean> TEST = new SimpleConfigEntry<Boolean>("test", true, "# ===[ PluginBase Test ]===");
    public static final ConfigEntry<Null> SETTINGS = new SimpleConfigEntry<Null>("settings", null, "# ===[ PluginBase Settings ]===");
    
    public MockConfig(BukkitPlugin plugin) throws IOException {
        super(plugin);
    }

    @Override
    protected ConfigEntry getSettingsEntry() {
       return SETTINGS;
    }

    @Override
    protected String getHeader() {
        return "# ===[ PluginBase Config ]===";
    }
}