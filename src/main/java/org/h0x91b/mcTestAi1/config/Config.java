package org.h0x91b.mcTestAi1.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public int getClassroomWidth() {
        return plugin.getConfig().getInt("classroom.width", 15);
    }

    public int getClassroomLength() {
        return plugin.getConfig().getInt("classroom.length", 10);
    }

    public int getClassroomHeight() {
        return plugin.getConfig().getInt("classroom.height", 5);
    }

    public int getClassroomY() {
        return plugin.getConfig().getInt("classroom.y", 200);
    }

    public Location getDayLocation(World defaultWorld) {
        String worldName = plugin.getConfig().getString("day.location.world", defaultWorld.getName());
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Configured world '" + worldName + "' not found. Using default world.");
            world = defaultWorld;
        }
        double x = plugin.getConfig().getDouble("day.location.x", -36);
        double y = plugin.getConfig().getDouble("day.location.y", 76);
        double z = plugin.getConfig().getDouble("day.location.z", 60);
        return new Location(world, x, y, z);
    }
}