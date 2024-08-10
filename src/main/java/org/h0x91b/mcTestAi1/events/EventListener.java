package org.h0x91b.mcTestAi1.events;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.config.Config;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

public class EventListener implements Listener {
    private final ClassroomManager classroomManager;
    private final DayNightManager dayNightManager;
    private final JavaPlugin plugin;
    private final Config config;

    @Inject
    public EventListener(ClassroomManager classroomManager, DayNightManager dayNightManager, JavaPlugin plugin, Config config) {
        this.classroomManager = classroomManager;
        this.dayNightManager = dayNightManager;
        this.plugin = plugin;
        this.config = config;
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
            } else {
                Location dayLocation = config.getDayLocation(Bukkit.getWorlds().get(0));
                event.getPlayer().teleport(dayLocation);
                event.getPlayer().sendMessage("Welcome! It's currently day, so you've been teleported to the day location.");
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
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Location dayLocation = config.getDayLocation(Bukkit.getWorlds().get(0));
                    event.getEntity().teleport(dayLocation);
                    event.getEntity().sendMessage("You died during the day, so you've been teleported to the day location.");
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error teleporting player on death: " + e.getMessage());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location blockLocation = event.getBlock().getLocation();
        if (classroomManager.isClassroomBlock(blockLocation)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Эй, братан! В этой комнате нельзя строить!");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        try {
            if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
                return; // We only care about player-vs-player damage
            }

            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (classroomManager.isClassroomBlock(victim.getLocation())) {
                event.setCancelled(true);
                attacker.sendMessage("ПвП запрещено в классной комнате!");
                plugin.getLogger().info("Prevented PvP damage in classroom: " + attacker.getName() + " -> " + victim.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling PvP event: " + e.getMessage());
        }
    }
}