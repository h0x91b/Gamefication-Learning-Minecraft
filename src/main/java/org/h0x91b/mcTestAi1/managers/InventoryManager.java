package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.config.Config;

public class InventoryManager {
    private final Config config;

    @Inject
    public InventoryManager(Config config) {
        this.config = config;
    }

    public void lockInventory(Player player) {
        // Реализация блокировки инвентаря
    }

    public void unlockInventory(Player player) {
        // Реализация разблокировки инвентаря
    }

    // Добавь остальные методы для управления инвентарём
}