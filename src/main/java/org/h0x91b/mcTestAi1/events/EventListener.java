package org.h0x91b.mcTestAi1.events;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EventListener implements Listener {
    private final ClassroomManager classroomManager;
    private final DayNightManager dayNightManager;
    private final JavaPlugin plugin;

    @Inject
    public EventListener(ClassroomManager classroomManager, DayNightManager dayNightManager, JavaPlugin plugin) {
        this.classroomManager = classroomManager;
        this.dayNightManager = dayNightManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (classroomManager.isClassroomBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Эй, братан! Эту комнату нельзя ломать!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            if (dayNightManager.isNight() && classroomManager.isClassroomCreated()) {
                dayNightManager.teleportPlayerToClassroom(event.getPlayer());
                event.getPlayer().sendMessage("Welcome! It's currently night, so you've been teleported to the classroom.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error teleporting player on join: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        try {
            if (dayNightManager.isNight() && classroomManager.isClassroomCreated()) {
                // Schedule the teleportation for the next tick to ensure the player has respawned
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    dayNightManager.teleportPlayerToClassroom(event.getEntity());
                    event.getEntity().sendMessage("You died during the night, so you've been teleported to the classroom.");
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error teleporting player on death: " + e.getMessage());
        }
    }
}