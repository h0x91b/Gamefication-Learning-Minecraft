package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.config.Config;

import java.util.ArrayList;
import java.util.List;

public class QuizManager {
    private final Config config;
    private List<ArmorStand> holograms = new ArrayList<>();
    private List<Location> quizButtonLocations = new ArrayList<>();

    @Inject
    public QuizManager(Config config) {
        this.config = config;
    }

    public void setupQuiz(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        addQuizButtons(world, roomLoc, totalWidth, totalLength, totalHeight);
        addHologram(world, roomLoc, totalWidth, totalHeight);
    }

    private void addQuizButtons(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        String[] options = {"A", "B", "C", "D"};
        quizButtonLocations.clear();
        double startX = roomLoc.getX() + totalWidth / 2.0 - 3;
        for (int i = 0; i < 4; i++) {
            Location buttonLoc = new Location(world,
                    startX + (i * 2),
                    roomLoc.getY() + 2,
                    roomLoc.getZ() + 1);
            addButton(world, buttonLoc, BlockFace.SOUTH, options[i]);
            addSignAboveButton(world, buttonLoc.clone().add(0, 1, 0), options[i]);
            quizButtonLocations.add(buttonLoc);
        }
    }

    private void addButton(World world, Location loc, BlockFace face, String option) {
        Block block = world.getBlockAt(loc);
        block.setType(Material.OAK_BUTTON);
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(face);
            block.setBlockData(blockData);
        }
    }

    private void addSignAboveButton(World world, Location loc, String option) {
        Block block = world.getBlockAt(loc);
        block.setType(Material.OAK_WALL_SIGN);
        BlockData blockData = block.getBlockData();
        if (blockData instanceof org.bukkit.block.data.type.WallSign) {
            ((org.bukkit.block.data.type.WallSign) blockData).setFacing(BlockFace.SOUTH);
            block.setBlockData(blockData);
        }
        Sign sign = (Sign) block.getState();
        sign.setLine(1, option);
        sign.update();
    }

    private void addHologram(World world, Location roomLoc, int totalWidth, int totalHeight) {
        // Удаляем старые голограммы, если они есть
        for (ArmorStand stand : holograms) {
            stand.remove();
        }
        holograms.clear();

        // Создаем новые голограммы
        double startY = roomLoc.getY() + totalHeight - 5; // Опускаем ниже
        for (int i = 0; i < 5; i++) { // Создаем 5 строк
            Location hologramLoc = new Location(world,
                    roomLoc.getX() + totalWidth / 2.0,
                    startY - (i * 0.3), // Каждая строка немного ниже предыдущей
                    roomLoc.getZ() + 2);
            ArmorStand hologram = (ArmorStand) world.spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
            hologram.setVisible(false);
            hologram.setGravity(false);
            hologram.setCustomNameVisible(true);
            holograms.add(hologram);
        }
        updateHologram(1); // Инициализируем текст
    }

    public void updateHologram(int count) {
        if (holograms.isEmpty()) return;

        String[] lines = {
                ChatColor.GOLD + "" + ChatColor.BOLD + "Счетчик: " + count,
                ChatColor.YELLOW + "Это охуенный охуенный охуенный охуенный охуенный охуенный охуенный охуенный охуенный охуенный",
                ChatColor.YELLOW + "плавающий текст",
                ChatColor.YELLOW + "который обновляется",
                ChatColor.YELLOW + "каждую секунду!"
        };

        for (int i = 0; i < holograms.size(); i++) {
            ArmorStand hologram = holograms.get(i);
            if (hologram.isValid()) {
                hologram.setCustomName(lines[i]);
            }
        }
    }

    public void handleQuizAnswer(Player player, Location buttonLoc) {
        int index = -1;
        for (int i = 0; i < quizButtonLocations.size(); i++) {
            if (isSameLocation(buttonLoc, quizButtonLocations.get(i))) {
                index = i;
                break;
            }
        }
        String answer = (index >= 0 && index < 4) ? "ABCD".charAt(index) + "" : "Неизвестно";

        player.sendMessage("Ты выбрал ответ: " + answer);

        // Здесь можно добавить логику проверки правильности ответа
        // и начисления очков
    }

    private boolean isSameLocation(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
                loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    public void removeQuizElements() {
        // Убираем голограммы
        for (ArmorStand hologram : holograms) {
            hologram.remove();
        }
        holograms.clear();

        // Убираем кнопки и таблички
        for (Location loc : quizButtonLocations) {
            loc.getBlock().setType(Material.AIR);
            loc.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
        }
        quizButtonLocations.clear();
    }
}