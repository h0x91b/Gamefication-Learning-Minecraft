package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import org.bukkit.World;
import org.h0x91b.mcTestAi1.config.Config;

public class DayNightManager {
    private final Config config;

    @Inject
    public DayNightManager(Config config) {
        this.config = config;
    }

    public void startDayNightCycle(World world) {
        // Реализация цикла дня и ночи
    }

    // Добавь остальные методы для управления циклом дня и ночи
}