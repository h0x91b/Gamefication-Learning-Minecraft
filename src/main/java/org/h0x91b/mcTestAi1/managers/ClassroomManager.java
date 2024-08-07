package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.config.Config;

import java.util.HashSet;
import java.util.Set;

public class ClassroomManager {
    private final Config config;
    private final Set<Location> classroomBlocks = new HashSet<>();
    private Location classroomLocation;

    @Inject
    public ClassroomManager(Config config) {
        this.config = config;
    }

    public void createRoom(Player player) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();

        int innerWidth = config.getClassroomWidth();
        int innerLength = config.getClassroomLength();
        int innerHeight = config.getClassroomHeight();

        int totalWidth = innerWidth + 2;
        int totalLength = innerLength + 2;
        int totalHeight = innerHeight + 2;

        classroomLocation = new Location(world, playerLoc.getX(), config.getClassroomY(), playerLoc.getZ());

        clearSpace(world, classroomLocation, totalWidth, totalLength, totalHeight);
        createWalls(world, classroomLocation, totalWidth, totalLength, totalHeight);
        createFloorAndCeiling(world, classroomLocation, totalWidth, totalLength, totalHeight);
        addTorches(world, classroomLocation, totalWidth, totalLength, totalHeight);
        addBlackboard(world, classroomLocation, totalWidth, totalHeight);

        // Телепортируем игрока в центр комнаты
        Location teleportLoc = new Location(world, classroomLocation.getX() + totalWidth / 2.0, classroomLocation.getY() + 1, classroomLocation.getZ() + totalLength / 2.0);
        player.teleport(teleportLoc);
        player.sendMessage("Йоу, братан! Твой новый класс " + innerWidth + "x" + innerLength + "x" + innerHeight + " готов, ты телепортирован!");
    }

    private void clearSpace(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        int t = 30;
        for (int x = -t; x < totalWidth + t; x++) {
            for (int y = -t; y < totalHeight + t; y++) {
                for (int z = -t; z < totalLength + t; z++) {
                    world.getBlockAt(roomLoc.getBlockX() + x, roomLoc.getBlockY() + y, roomLoc.getBlockZ() + z).setType(Material.AIR);
                }
            }
        }
    }

    private void createWalls(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        Material wallMaterial = Material.STONE_BRICKS;
        for (int x = 0; x < totalWidth; x++) {
            for (int y = 0; y < totalHeight; y++) {
                for (int z = 0; z < totalLength; z++) {
                    if (x == 0 || x == totalWidth - 1 || y == 0 || y == totalHeight - 1 || z == 0 || z == totalLength - 1) {
                        Block block = world.getBlockAt(roomLoc.getBlockX() + x, roomLoc.getBlockY() + y, roomLoc.getBlockZ() + z);
                        block.setType(wallMaterial);
                        classroomBlocks.add(block.getLocation());
                    }
                }
            }
        }
    }

    private void createFloorAndCeiling(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        Material floorMaterial1 = Material.GLASS;
        Material floorMaterial2 = Material.OAK_PLANKS;
        Material ceilingMaterial = Material.GLASS;

        for (int x = 1; x < totalWidth - 1; x++) {
            for (int z = 1; z < totalLength - 1; z++) {
                Block floorBlock = world.getBlockAt(roomLoc.getBlockX() + x, roomLoc.getBlockY(), roomLoc.getBlockZ() + z);
                Material floorMaterial = ((x + z) % 2 == 0) ? floorMaterial1 : floorMaterial2;
                floorBlock.setType(floorMaterial);
                classroomBlocks.add(floorBlock.getLocation());

                Block ceilingBlock = world.getBlockAt(roomLoc.getBlockX() + x, roomLoc.getBlockY() + totalHeight - 1, roomLoc.getBlockZ() + z);
                ceilingBlock.setType(ceilingMaterial);
                classroomBlocks.add(ceilingBlock.getLocation());
            }
        }
    }

    private void addTorches(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        for (int x = 2; x < totalWidth - 2; x += 3) {
            addTorch(world, roomLoc.clone().add(x, totalHeight - 2, 0), BlockFace.SOUTH);
            addTorch(world, roomLoc.clone().add(x, totalHeight - 2, totalLength - 1), BlockFace.NORTH);
        }
        for (int z = 2; z < totalLength - 2; z += 3) {
            addTorch(world, roomLoc.clone().add(0, totalHeight - 2, z), BlockFace.EAST);
            addTorch(world, roomLoc.clone().add(totalWidth - 1, totalHeight - 2, z), BlockFace.WEST);
        }
    }

    private void addTorch(World world, Location loc, BlockFace face) {
        Block block = world.getBlockAt(loc);
        block.setType(Material.WALL_TORCH);
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(face);
            block.setBlockData(blockData);
        }
        classroomBlocks.add(block.getLocation());
    }

    private void addBlackboard(World world, Location roomLoc, int totalWidth, int totalHeight) {
        Material boardMaterial = Material.BLACK_CONCRETE;
        int boardWidth = 7;
        int boardHeight = 3;
        int startX = (totalWidth - boardWidth) / 2;
        int startY = totalHeight - boardHeight - 1;

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                Block block = world.getBlockAt(roomLoc.getBlockX() + startX + x, roomLoc.getBlockY() + startY + y, roomLoc.getBlockZ());
                block.setType(boardMaterial);
                classroomBlocks.add(block.getLocation());
            }
        }
    }

    public Location getClassroomLocation() {
        if (classroomLocation == null) {
            throw new IllegalStateException("Класс ещё не создан, братан!");
        }
        return classroomLocation.clone().add(1, 1, 1); // Возвращаем локацию внутри класса
    }

    public boolean isClassroomBlock(Location location) {
        return classroomBlocks.contains(location);
    }
}