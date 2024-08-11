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
import org.h0x91b.mcTestAi1.commands.LanguageCommand;
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
            binder.bind(LanguageCommand.class).asEagerSingleton();
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

        // Initialize the quiz after the classroom is created and day/night cycle is started
        quizManager.resetQuestionPool();
        if (dayNightManager.isNight()) {
            quizManager.startQuiz();
        }

        // Register the new language command
        LanguageCommand languageCommand = injector.getInstance(LanguageCommand.class);
        getCommand("language").setExecutor(languageCommand);

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
        if (classroomManager != null && classroomManager.isClassroomCreated()) {
            try {
                classroomManager.cleanupClassroomContents();
            } catch (Exception e) {
                getLogger().severe("Error during final classroom cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        }
        getLogger().info("mcTestAi1 плагин выключается. Пока, пацаны!");
    }
}