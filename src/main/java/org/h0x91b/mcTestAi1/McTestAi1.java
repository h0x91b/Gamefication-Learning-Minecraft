package org.h0x91b.mcTestAi1;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.events.EventListener;
import org.h0x91b.mcTestAi1.listeners.ButtonClickListener;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;
import org.h0x91b.mcTestAi1.managers.InventoryManager;
import org.h0x91b.mcTestAi1.managers.QuizManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.Material;

public final class McTestAi1 extends JavaPlugin {
    private Injector injector;
    private DayNightManager dayNightManager;
    private ClassroomManager classroomManager;
    private QuizManager quizManager;

    @Override
    public void onEnable() {
        // Инициализация Guice
        this.injector = Guice.createInjector(binder -> {
            binder.bind(JavaPlugin.class).toInstance(this);
            binder.bind(Config.class).toProvider(() -> new Config(this)).asEagerSingleton();
            binder.bind(ClassroomManager.class).asEagerSingleton();
            binder.bind(QuizManager.class).asEagerSingleton();
            binder.bind(DayNightManager.class).asEagerSingleton();
            binder.bind(InventoryManager.class).asEagerSingleton();
            binder.bind(EventListener.class).asEagerSingleton();
            binder.bind(ButtonClickListener.class).asEagerSingleton();
        });

        // Инициализация менеджеров
        this.classroomManager = injector.getInstance(ClassroomManager.class);
        this.dayNightManager = injector.getInstance(DayNightManager.class);
        this.quizManager = injector.getInstance(QuizManager.class);
        injector.getInstance(InventoryManager.class);

        // Создаем комнату автоматически
        createClassroomIfNotExists();

        // Регистрация обработчиков событий
        getServer().getPluginManager().registerEvents(injector.getInstance(EventListener.class), this);
        getServer().getPluginManager().registerEvents(injector.getInstance(ButtonClickListener.class), this);

        // Запуск цикла дня и ночи
        startDayNightCycle();

        getLogger().info("mcTestAi1 плагин загружен и готов отжигать!");
    }

    private void createClassroomIfNotExists() {
        if (!classroomManager.isClassroomCreated()) {
            World world = getServer().getWorlds().get(0);
            Location spawnLocation = world.getSpawnLocation();
            
            // Создаем комнату на высоте Y=200, используя координаты X и Z точки спавна
            Location classroomLocation = new Location(world, spawnLocation.getX(), 200, spawnLocation.getZ());
            
            classroomManager.createRoom(classroomLocation);
            getLogger().info("Автоматически создана классная комната!");
        }
    }

    private void startDayNightCycle() {
        World world = getServer().getWorlds().get(0);
        if (dayNightManager.canStartCycle()) {
            dayNightManager.startDayNightCycle(world);
            getLogger().info("День-ночь цикл успешно запущен!");
        } else {
            getLogger().warning("День-ночь цикл не может быть запущен. Проверьте, создана ли классная комната.");
        }
    }

    @Override
    public void onDisable() {
        if (dayNightManager != null) {
            dayNightManager.stopTimer();
        }
        if (quizManager != null) {
            quizManager.removeAllHolograms();
            quizManager.cleanupQuiz();
        }
        if (classroomManager != null) {
            cleanupClassroom();
        }
        getLogger().info("mcTestAi1 плагин выключается. Пока, пацаны!");
    }

    private void cleanupClassroom() {
        if (!classroomManager.isClassroomCreated()) {
            return;
        }

        Location classroomLocation = classroomManager.getClassroomLocation();
        World world = classroomLocation.getWorld();
        int width = classroomManager.getClassroomWidth();
        int length = classroomManager.getClassroomLength();
        int height = classroomManager.getClassroomHeight();

        // Define the classroom boundaries
        int minX = classroomLocation.getBlockX();
        int minY = classroomLocation.getBlockY();
        int minZ = classroomLocation.getBlockZ();
        int maxX = minX + width;
        int maxY = minY + height;
        int maxZ = minZ + length;

        // Remove all entities within the classroom boundaries
        for (Entity entity : world.getEntities()) {
            Location loc = entity.getLocation();
            if (loc.getBlockX() >= minX && loc.getBlockX() < maxX &&
                loc.getBlockY() >= minY && loc.getBlockY() < maxY &&
                loc.getBlockZ() >= minZ && loc.getBlockZ() < maxZ) {
                
                // Remove all entities except players
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }

        // Remove all non-structural blocks (like buttons, signs, etc.)
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!classroomManager.isStructuralBlock(block)) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        classroomManager.cleanup();
    }
}