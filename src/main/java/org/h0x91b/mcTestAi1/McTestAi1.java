package org.h0x91b.mcTestAi1;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.events.EventListener;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;
import org.h0x91b.mcTestAi1.managers.InventoryManager;
import org.h0x91b.mcTestAi1.managers.QuizManager;

public final class McTestAi1 extends JavaPlugin {
    private Injector injector;
    private DayNightManager dayNightManager;
    private ClassroomManager classroomManager;

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
        });

        // Инициализация менеджеров
        this.classroomManager = injector.getInstance(ClassroomManager.class);
        this.dayNightManager = injector.getInstance(DayNightManager.class);
        injector.getInstance(QuizManager.class);
        injector.getInstance(InventoryManager.class);

        // Создаем комнату автоматически
        createClassroomIfNotExists();

        // Регистрация обработчиков событий
        getServer().getPluginManager().registerEvents(injector.getInstance(EventListener.class), this);

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
        getLogger().info("mcTestAi1 плагин выключается. Пока, пацаны!");
    }
}