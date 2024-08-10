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
        Player player = event.getPlayer();
        plugin.getLogger().info("Игрок " + player.getName() + " присоединяется. Попытка телепортации...");

        // Schedule teleportation for the next tick to ensure the player is fully loaded
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (dayNightManager.isNight() && classroomManager.isClassroomCreated()) {
                    Location classroomLocation = classroomManager.getClassroomLocation();
                    player.teleport(classroomLocation);
                    plugin.getLogger().info("Телепортирован " + player.getName() + " в класс на координаты " + locationToString(classroomLocation));
                    player.sendMessage("Добро пожаловать! Вы были телепортированы в класс.");
                } else {
                    Location dayLocation = config.getDayLocation(player.getWorld());
                    player.teleport(dayLocation);
                    plugin.getLogger().info("Телепортирован " + player.getName() + " в дневную локацию на координаты " + locationToString(dayLocation));
                    player.sendMessage("Добро пожаловать! Вы были телепортированы к дому");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при телепортации игрока " + player.getName() + " при входе: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getLogger().info("Игрок " + player.getName() + " умер. Планирование телепортации...");

        // Schedule teleportation for 1 second (20 ticks) later to ensure the player has respawned
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                boolean isNight = dayNightManager.isNight();
                boolean isClassroomCreated = classroomManager.isClassroomCreated();
                plugin.getLogger().info("Состояние: Ночь - " + isNight + ", Класс создан - " + isClassroomCreated);

                if (isNight && isClassroomCreated) {
                    Location classroomLocation = classroomManager.getClassroomLocation();
                    if (classroomLocation != null) {
                        player.teleport(classroomLocation);
                        plugin.getLogger().info("Телепортирован " + player.getName() + " в класс после смерти на координаты " + locationToString(classroomLocation));
                        player.sendMessage("Вы умерли ночью, поэтому были телепортированы в класс.");
                    } else {
                        plugin.getLogger().severe("Не удалось получить местоположение класса. Телепортация не выполнена.");
                        teleportToFallbackLocation(player);
                    }
                } else {
                    Location dayLocation = config.getDayLocation(player.getWorld());
                    player.teleport(dayLocation);
                    plugin.getLogger().info("Телепортирован " + player.getName() + " в дневную локацию после смерти на координаты " + locationToString(dayLocation));
                    player.sendMessage("Вы умерли днем, поэтому были телепортированы в дневную локацию.");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при телепортации игрока " + player.getName() + " после смерти: " + e.getMessage());
                e.printStackTrace();
                teleportToFallbackLocation(player);
            }
        }, 20L); // 20 ticks = 1 second
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

    private void teleportToFallbackLocation(Player player) {
        try {
            Location spawnLocation = player.getWorld().getSpawnLocation();
            player.teleport(spawnLocation);
            plugin.getLogger().info("Игрок " + player.getName() + " телепортирован на точку возрождения из-за ошибки.");
            player.sendMessage("Произошла ошибка при телепортации. Вы были перемещены на точку возрождения.");
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось телепортировать игрока " + player.getName() + " на точку возрождения: " + e.getMessage());
        }
    }

    private String locationToString(Location location) {
        return String.format("(%.2f, %.2f, %.2f)", location.getX(), location.getY(), location.getZ());
    }
}