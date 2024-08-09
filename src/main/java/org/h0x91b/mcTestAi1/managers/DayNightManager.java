package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.h0x91b.mcTestAi1.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DayNightManager {
    private final JavaPlugin plugin;
    private final Config config;
    private final QuizManager quizManager;
    private final ClassroomManager classroomManager;
    private final Logger logger;

    private boolean isNight = false;
    private int dayDuration = 30; // начальная длительность дня в секундах
    private final int nightDuration = 60; // длительность ночи в секундах
    private Map<UUID, Integer> playerScores = new HashMap<>();
    private Map<UUID, Location> playerLocations = new HashMap<>();

    private int remainingTime;
    private BukkitTask timerTask;
    private BukkitTask countdownTask;

    @Inject
    public DayNightManager(JavaPlugin plugin, Config config, QuizManager quizManager, ClassroomManager classroomManager) {
        this.plugin = plugin;
        this.config = config;
        this.quizManager = quizManager;
        this.classroomManager = classroomManager;
        this.logger = plugin.getLogger();
    }

    public boolean canStartCycle() {
        return classroomManager.isClassroomCreated();
    }

    public void startDayNightCycle(World world) {
        if (!canStartCycle()) {
            plugin.getLogger().severe("День-ночь цикл не может начаться, потому что класс ещё не создан!");
            return;
        }

        plugin.getLogger().info("Запускаем цикл дня и ночи, епта!");
        startNight(world);
    }

    private void startDay(World world) {
        isNight = false;
        world.setTime(0); // установка времени на рассвет
        Bukkit.broadcastMessage("Наступил день! Погнали на волю!");
        if (quizManager != null) {
            quizManager.removeAllHolograms(); // Ensure thorough cleanup of holograms
            quizManager.endQuiz();
        }
        teleportPlayersToDayLocation();

        // Расчёт новой длительности дня
        int totalScore = playerScores.values().stream().mapToInt(Integer::intValue).sum();
        dayDuration = 30 + (totalScore * 30); // 30 секунд + 30 секунд за каждый правильный ответ
        playerScores.clear(); // сбрасываем счёт

        remainingTime = dayDuration;
        startTimer();
    }

    private void startNight(World world) {
        isNight = true;
        world.setTime(13000); // установка времени на ночь
        Bukkit.broadcastMessage("Наступила ночь, народ! Погнали на уроки!");

        try {
            logger.info("Starting classroom cleanup...");
            classroomManager.cleanupClassroomContents();
            quizManager.cleanupQuiz();
            logger.info("Classroom cleanup completed.");
        } catch (Exception e) {
            logger.severe("Error cleaning up classroom contents: " + e.getMessage());
            e.printStackTrace();
            // Continue with the night cycle even if cleanup fails
        }

        teleportPlayersToClassroom();

        remainingTime = nightDuration;
        startTimer();

        quizManager.startQuiz();
    }

    private void startTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingTime > 10) {
                if (remainingTime % 10 == 0) {
                    broadcastRemainingTime(remainingTime);
                }
                remainingTime -= 10;
            } else {
                timerTask.cancel();
                startFinalCountdown();
            }
        }, 0L, 200L); // 200 ticks = 10 seconds
    }

    private void startFinalCountdown() {
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingTime > 0) {
                if(remainingTime <= 5 || remainingTime == 10) broadcastRemainingTime(remainingTime);
                remainingTime--;
            } else {
                countdownTask.cancel();
                broadcastRemainingTime(0);
                
                // Запускаем смену дня/ночи в следующем тике
                Bukkit.getScheduler().runTask(plugin, () -> {
                    World world = Bukkit.getWorlds().get(0);
                    if (isNight) {
                        startDay(world);
                    } else {
                        startNight(world);
                    }
                });
            }
        }, 0L, 20L); // 20 ticks = 1 second
    }

    private void broadcastRemainingTime(int time) {
        if (time > 0) {
            String message = ChatColor.YELLOW + "Осталось " + time + " " + 
                             (time == 1 ? "секунда" : (time < 5 ? "секунды" : "секунд")) + 
                             " до " + (isNight ? "дня" : "ночи") + "!";
            Bukkit.broadcastMessage(message);
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "Внимание! Сейчас начнется " + (isNight ? "день" : "ночь") + "!");
        }
    }

    public void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
    }

    private void teleportPlayersToClassroom() {
        if (!classroomManager.isClassroomCreated()) {
            plugin.getLogger().warning("Не удалось телепортировать игроков в класс, так как он ещё не создан!");
            return;
        }

        Location classroomLocation = classroomManager.getClassroomLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerLocations.put(player.getUniqueId(), player.getLocation());
            player.teleport(classroomLocation);
        }
    }

    private void teleportPlayersToDayLocation() {
        World targetWorld = Bukkit.getWorlds().get(0); // Get the main world
        Location dayLocation = config.getDayLocation(targetWorld);

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.teleport(dayLocation);
                logger.info("Teleported player " + player.getName() + " to day location: " + dayLocation);
            } catch (Exception e) {
                logger.severe("Failed to teleport player " + player.getName() + " to day location: " + e.getMessage());
            }
        }
        playerLocations.clear(); // Clear saved locations as they're no longer needed
    }

    public int addScore(UUID playerId, int score) {
        int currentScore = playerScores.getOrDefault(playerId, 0);
        int newScore = currentScore + score;
        playerScores.put(playerId, newScore);
        return calculateAdditionalTime(score);
    }

    public boolean isNight() {
        return isNight;
    }

    public void teleportPlayerToClassroom(Player player) {
        if (!classroomManager.isClassroomCreated()) {
            plugin.getLogger().warning("Cannot teleport player to classroom: Classroom not created");
            return;
        }

        Location classroomLocation = classroomManager.getClassroomLocation();
        if (classroomLocation == null) {
            plugin.getLogger().warning("Cannot teleport player to classroom: Invalid classroom location");
            return;
        }

        playerLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(classroomLocation);
    }

    public int calculateAdditionalTime(int score) {
        return score * 30; // 30 секунд за каждый правильный ответ
    }
}