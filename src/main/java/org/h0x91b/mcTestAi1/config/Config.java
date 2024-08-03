package org.h0x91b.mcTestAi1.config;

import org.bukkit.plugin.java.JavaPlugin;

public class Config {
    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public int getClassroomWidth() {
        return plugin.getConfig().getInt("classroom.width", 15);
    }

    public int getClassroomLength() {
        return plugin.getConfig().getInt("classroom.length", 10);
    }

    public int getClassroomHeight() {
        return plugin.getConfig().getInt("classroom.height", 5);
    }

    public int getClassroomY() {
        return plugin.getConfig().getInt("classroom.y", 200);
    }

    // Добавь остальные конфиг-методы по необходимости
}