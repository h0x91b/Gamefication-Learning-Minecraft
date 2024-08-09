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
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.config.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ClassroomManager {
    private final Config config;
    private final Set<Location> classroomBlocks = new HashSet<>();
    private Location classroomLocation;
    private List<Location> quizButtonLocations = new ArrayList<>();
    private final Logger logger;

    @Inject
    public ClassroomManager(JavaPlugin plugin, Config config) {
        this.config = config;
        this.logger = plugin.getLogger();
    }

    public void createRoom(Location location) {
        cleanupExistingClassroom();

        World world = location.getWorld();

        int innerWidth = config.getClassroomWidth();
        int innerLength = config.getClassroomLength();
        int innerHeight = config.getClassroomHeight();

        int totalWidth = innerWidth + 2;
        int totalLength = innerLength + 2;
        int totalHeight = innerHeight + 2;

        classroomLocation = location;

        clearSpace(world, classroomLocation, totalWidth, totalLength, totalHeight);
        createWalls(world, classroomLocation, totalWidth, totalLength, totalHeight);
        createFloorAndCeiling(world, classroomLocation, totalWidth, totalLength, totalHeight);
        addTorches(world, classroomLocation, totalWidth, totalLength, totalHeight);
        addBlackboard(world, classroomLocation, totalWidth, totalHeight);
        addQuizButtons(world, classroomLocation, totalWidth, totalLength, totalHeight);
    }

    public void createRoom(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        Location playerLoc = player.getLocation();
        Location roomLoc = new Location(player.getWorld(), playerLoc.getX(), config.getClassroomY(), playerLoc.getZ());
        createRoom(roomLoc);

        // Телепортируем игрока в центр комнаты
        Location teleportLoc = getClassroomLocation();
        player.teleport(teleportLoc);
        player.sendMessage("Йоу, братан! Твой новый класс " + config.getClassroomWidth() + "x" + config.getClassroomLength() + "x" + config.getClassroomHeight() + " готов, ты телепортирован!");
    }

    private void cleanupExistingClassroom() {
        if (classroomLocation == null) return;

        World world = classroomLocation.getWorld();
        if (world == null) return;

        for (Location location : classroomBlocks) {
            Block block = world.getBlockAt(location);
            if (isClassroomBlock(location)) {
                block.setType(Material.AIR);
            }
        }

        classroomBlocks.clear();
        quizButtonLocations.clear();
        classroomLocation = null;
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

    private void addQuizButtons(World world, Location roomLoc, int totalWidth, int totalLength, int totalHeight) {
        quizButtonLocations.clear(); // Clear existing button locations
        int buttonY = 2; // Height at which to place buttons
        int spacing = 2; // Spaces between buttons
        int numButtons = 4; // Number of buttons (A, B, C, D)

        int totalButtonWidth = (numButtons * 1) + ((numButtons - 1) * spacing); // 1 block per button + spaces
        int startX = (totalWidth - totalButtonWidth) / 2; // Center the buttons

        // Place buttons on the front wall
        for (int i = 0; i < numButtons; i++) {
            int x = startX + (i * (1 + spacing));
            Location buttonLoc = new Location(world, roomLoc.getBlockX() + x, roomLoc.getBlockY() + buttonY, roomLoc.getBlockZ() + 1);
            addQuizButton(world, buttonLoc, BlockFace.SOUTH, i);
        }
        
        logger.info("Quiz buttons added. Total buttons: " + quizButtonLocations.size());
    }

    private void addQuizButton(World world, Location location, BlockFace facing, int buttonIndex) {
        Block buttonBlock = world.getBlockAt(location);
        buttonBlock.setType(Material.OAK_BUTTON);
        BlockData blockData = buttonBlock.getBlockData();
        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(facing);
            buttonBlock.setBlockData(blockData);
        }
        quizButtonLocations.add(location);
        classroomBlocks.add(location);

        logger.info("Added quiz button at location: " + location);

        Block signBlock = world.getBlockAt(location.clone().add(0, 1, 0));
        signBlock.setType(Material.OAK_WALL_SIGN);
        BlockData signData = signBlock.getBlockData();
        if (signData instanceof WallSign) {
            ((WallSign) signData).setFacing(facing);
            signBlock.setBlockData(signData);
        }
        if (signBlock.getState() instanceof Sign) {
            Sign sign = (Sign) signBlock.getState();
            sign.setLine(0, ChatColor.BOLD + String.valueOf((char)('A' + buttonIndex)));
            sign.setLine(1, "");
            sign.update(true);
        }
        classroomBlocks.add(signBlock.getLocation());
    }

    public boolean isClassroomCreated() {
        return classroomLocation != null;
    }

    public Location getClassroomLocation() {
        if (classroomLocation == null) {
            throw new IllegalStateException("Класс ещё не создан, братан!");
        }
        int width = config.getClassroomWidth();
        int length = config.getClassroomLength();
        int height = config.getClassroomHeight();

        // Вычисляем центр комнаты
        double centerX = classroomLocation.getX() + (width / 2.0) + 1;
        double centerY = classroomLocation.getY() + 1;
        double centerZ = classroomLocation.getZ() + (length / 2.0) + 1;

        return new Location(classroomLocation.getWorld(), centerX, centerY, centerZ);
    }

    public boolean isClassroomBlock(Location location) {
        return classroomBlocks.contains(location) ||
                quizButtonLocations.contains(location) ||
                isButtonOrSign(location.getBlock());
    }

    private boolean isButtonOrSign(Block block) {
        return block.getType() == Material.OAK_BUTTON ||
                block.getType() == Material.OAK_WALL_SIGN;
    }

    public List<Location> getQuizButtonLocations() {
        logger.info("Retrieving quiz button locations: " + quizButtonLocations);
        return new ArrayList<>(quizButtonLocations);
    }

    public void cleanup() {
        cleanupExistingClassroom();
    }

    public boolean isStructuralBlock(Block block) {
        Material type = block.getType();
        return type == Material.STONE_BRICKS || // Walls
               type == Material.GLASS || // Floor and ceiling
               type == Material.OAK_PLANKS || // Floor
               type == Material.BLACK_CONCRETE; // Blackboard
    }

    public int getClassroomWidth() {
        return config.getClassroomWidth() + 2; // Add 2 for walls
    }

    public int getClassroomLength() {
        return config.getClassroomLength() + 2; // Add 2 for walls
    }

    public int getClassroomHeight() {
        return config.getClassroomHeight() + 2; // Add 2 for floor and ceiling
    }

    public void cleanupClassroomContents() {
        if (!isClassroomCreated()) {
            logger.warning("Attempted to clean up classroom contents, but classroom is not created.");
            return;
        }

        Location classroomLocation = getClassroomLocation();
        World world = classroomLocation.getWorld();
        int width = getClassroomWidth();
        int length = getClassroomLength();
        int height = getClassroomHeight();

        // Define the classroom boundaries
        int minX = classroomLocation.getBlockX();
        int minY = classroomLocation.getBlockY();
        int minZ = classroomLocation.getBlockZ();
        int maxX = minX + width;
        int maxY = minY + height;
        int maxZ = minZ + length;

        // Remove all entities within the classroom boundaries
        world.getEntities().stream()
            .filter(entity -> !(entity instanceof Player))
            .filter(entity -> entity.getLocation().getBlockX() >= minX && entity.getLocation().getBlockX() < maxX
                           && entity.getLocation().getBlockY() >= minY && entity.getLocation().getBlockY() < maxY
                           && entity.getLocation().getBlockZ() >= minZ && entity.getLocation().getBlockZ() < maxZ)
            .forEach(Entity::remove);

        // Remove all non-structural blocks
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!isStructuralBlock(block)) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        logger.info("Classroom contents cleaned up successfully.");
    }
}