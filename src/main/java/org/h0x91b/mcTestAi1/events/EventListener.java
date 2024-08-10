package org.h0x91b.mcTestAi1.events;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
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
    private static final int MAX_TELEPORT_ATTEMPTS = 3;
    private static final long TELEPORT_DELAY_TICKS = 100L; // 5 seconds
    private static final long LOCATION_CHECK_DELAY_TICKS = 40L; // 2 seconds after teleport

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

        // Schedule teleportation for 5 seconds (100 ticks) later to ensure the player has fully respawned
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                teleportPlayerAfterDeath(player, 1);
            } else {
                plugin.getLogger().warning("Player " + player.getName() + " is offline. Teleportation cancelled.");
            }
        }, TELEPORT_DELAY_TICKS);
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

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Location signLocation = event.getBlock().getLocation();

        if (classroomManager.isClassroomBlock(signLocation)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Изменение табличек в классной комнате запрещено!");
            plugin.getLogger().info("Prevented sign change by " + player.getName() + " in classroom at " + signLocation);
        }
    }

    private void teleportPlayerAfterDeath(Player player, int attempt) {
        try {
            boolean isNight = dayNightManager.isNight();
            boolean isClassroomCreated = classroomManager.isClassroomCreated();
            plugin.getLogger().info("Попытка телепортации " + attempt + ": Состояние: Ночь - " + isNight + ", Класс создан - " + isClassroomCreated);

            Location targetLocation;
            if (isNight && isClassroomCreated) {
                targetLocation = classroomManager.getClassroomLocation();
                if (targetLocation == null) {
                    plugin.getLogger().severe("Не удалось получить местоположение класса. Переход к запасному варианту.");
                    teleportToFallbackLocation(player);
                    return;
                }
            } else {
                targetLocation = config.getDayLocation(player.getWorld());
            }

            Location beforeTeleport = player.getLocation();
            plugin.getLogger().info("Текущее местоположение игрока " + player.getName() + ": " + locationToString(beforeTeleport));
            plugin.getLogger().info("Целевое местоположение: " + locationToString(targetLocation));

            player.teleport(targetLocation);
            plugin.getLogger().info("Телепортирован " + player.getName() + " на координаты " + locationToString(targetLocation));

            // Check player's location after a short delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location afterTeleport = player.getLocation();
                plugin.getLogger().info("Местоположение игрока " + player.getName() + " после телепортации: " + locationToString(afterTeleport));

                if (!isPlayerAtLocation(player, targetLocation)) {
                    plugin.getLogger().warning("Игрок " + player.getName() + " не находится в ожидаемом местоположении после телепортации.");
                    if (attempt < MAX_TELEPORT_ATTEMPTS) {
                        plugin.getLogger().info("Повторная попытка телепортации...");
                        teleportPlayerAfterDeath(player, attempt + 1);
                    } else {
                        plugin.getLogger().severe("Достигнуто максимальное количество попыток телепортации. Переход к запасному варианту.");
                        teleportToFallbackLocation(player);
                    }
                } else {
                    player.sendMessage("Вы были телепортированы после смерти.");
                }
            }, LOCATION_CHECK_DELAY_TICKS);

        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при телепортации игрока " + player.getName() + " после смерти: " + e.getMessage());
            e.printStackTrace();
            teleportToFallbackLocation(player);
        }
    }

    private boolean isPlayerAtLocation(Player player, Location targetLocation) {
        Location playerLoc = player.getLocation();
        return playerLoc.getWorld().equals(targetLocation.getWorld()) &&
               playerLoc.distanceSquared(targetLocation) < 4.0; // Allow for larger discrepancies (2 block radius)
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