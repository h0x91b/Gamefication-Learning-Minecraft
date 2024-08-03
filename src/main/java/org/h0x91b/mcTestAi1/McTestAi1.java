package org.h0x91b.mcTestAi1;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.commands.CreateRoomCommand;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.config.ConfigModule;
import org.h0x91b.mcTestAi1.events.EventListener;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;
import org.h0x91b.mcTestAi1.managers.InventoryManager;
import org.h0x91b.mcTestAi1.managers.QuizManager;

public final class McTestAi1 extends JavaPlugin {
    private Injector injector;

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
        ClassroomManager classroomManager = injector.getInstance(ClassroomManager.class);
        QuizManager quizManager = injector.getInstance(QuizManager.class);
        DayNightManager dayNightManager = injector.getInstance(DayNightManager.class);
        InventoryManager inventoryManager = injector.getInstance(InventoryManager.class);

        // Регистрация команд
        getCommand("createroom").setExecutor(injector.getInstance(CreateRoomCommand.class));

        // Регистрация обработчиков событий
        getServer().getPluginManager().registerEvents(injector.getInstance(EventListener.class), this);

        getLogger().info("mcTestAi1 плагин загружен и готов отжигать!");
    }

    @Override
    public void onDisable() {
        getLogger().info("mcTestAi1 плагин выключается. Пока, пацаны!");
    }
}