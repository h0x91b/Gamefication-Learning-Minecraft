package org.h0x91b.mcTestAi1.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.events.EventListener;

public class ConfigModule extends AbstractModule {
    private final JavaPlugin plugin;

    public ConfigModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(EventListener.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public Config provideConfig() {
        return new Config(plugin);
    }
}