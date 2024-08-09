package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.models.Question;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class QuizManager {
    private final Config config;
    private final List<ArmorStand> holograms = new ArrayList<>();
    private List<Location> quizButtonLocations;
    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();
    private Question currentQuestion;
    private final Provider<DayNightManager> dayNightManagerProvider;
    private final ClassroomManager classroomManager;
    private final Logger logger;

    @Inject
    public QuizManager(JavaPlugin plugin, Config config, Provider<DayNightManager> dayNightManagerProvider, ClassroomManager classroomManager) {
        this.config = config;
        this.dayNightManagerProvider = dayNightManagerProvider;
        this.classroomManager = classroomManager;
        this.logger = plugin.getLogger();
        initializeQuestions();
        updateQuizButtonLocations();
    }

    private void updateQuizButtonLocations() {
        this.quizButtonLocations = classroomManager.getQuizButtonLocations();
        logger.info("Updated quiz button locations: " + quizButtonLocations);
    }

    private void initializeQuestions() {
        questions.add(new Question("What is the capital of France?",
                List.of("London", "Berlin", "Paris", "Madrid"), 2));
        questions.add(new Question("Which planet is known as the Red Planet?",
                List.of("Venus", "Mars", "Jupiter", "Saturn"), 1));
        questions.add(new Question("What is the largest mammal in the world?",
                List.of("Elephant", "Blue Whale", "Giraffe", "Hippopotamus"), 1));
    }

    public void startQuiz() {
        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions available for the quiz!");
        }
        removeAllHolograms();
        updateQuizButtonLocations();
        currentQuestion = questions.get(random.nextInt(questions.size()));
        updateQuizButtons();
        updateHologramWithCurrentQuestion();
    }

    private void updateHologramWithCurrentQuestion() {
        removeExistingHolograms();

        if (currentQuestion == null || !classroomManager.isClassroomCreated()) {
            return;
        }

        Location classroomLocation = classroomManager.getClassroomLocation();
        World world = classroomLocation.getWorld();

        if (world == null) {
            return;
        }

        Location hologramLocation = classroomLocation.clone().add(0, 2, 1);

        createHologramLine(world, hologramLocation, ChatColor.GOLD + "Question:");
        createHologramLine(world, hologramLocation.clone().add(0, -0.25, 0), ChatColor.WHITE + currentQuestion.getQuestion());

        List<String> answers = currentQuestion.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            String answerPrefix = ChatColor.YELLOW.toString() + (char)('A' + i) + ": ";
            String answerText = ChatColor.WHITE + answers.get(i);
            createHologramLine(world, hologramLocation.clone().add(0, -0.5 - (0.25 * i), 0), answerPrefix + answerText);
        }
    }

    private void createHologramLine(World world, Location location, String text) {
        ArmorStand hologram = world.spawn(location, ArmorStand.class);
        hologram.setGravity(false);
        hologram.setCanPickupItems(false);
        hologram.setCustomName(text);
        hologram.setCustomNameVisible(true);
        hologram.setVisible(false);
        holograms.add(hologram);
    }

    private void removeExistingHolograms() {
        for (ArmorStand hologram : holograms) {
            if (hologram != null && hologram.isValid()) {
                hologram.remove();
            }
        }
        holograms.clear();
    }

    private void updateQuizButtons() {
        if (currentQuestion == null || quizButtonLocations.isEmpty()) return;

        for (int i = 0; i < quizButtonLocations.size() && i < currentQuestion.getAnswers().size(); i++) {
            Location buttonLoc = quizButtonLocations.get(i);
            String answerText = currentQuestion.getAnswers().get(i);
            String buttonLabel = String.valueOf((char)('A' + i));
            updateButtonSign(buttonLoc, buttonLabel, answerText);
        }
    }

    private void updateButtonSign(Location buttonLoc, String buttonLabel, String answerText) {
        Location signLoc = buttonLoc.clone().add(0, 1, 0);
        Block signBlock = signLoc.getBlock();
        if (signBlock.getState() instanceof Sign) {
            Sign sign = (Sign) signBlock.getState();
            sign.setLine(0, ChatColor.BOLD + buttonLabel);
            sign.setLine(1, answerText);
            sign.update(true);
        }
    }

    public void handleQuizAnswer(Player player, Location buttonLoc) {
        if (currentQuestion == null) {
            player.sendMessage(ChatColor.RED + "The quiz hasn't started yet!");
            return;
        }

        int index = -1;
        for (int i = 0; i < quizButtonLocations.size(); i++) {
            Location loc = quizButtonLocations.get(i);
            if (buttonLoc.getBlockX() == loc.getBlockX() &&
                buttonLoc.getBlockY() == loc.getBlockY() &&
                buttonLoc.getBlockZ() == loc.getBlockZ()) {
                index = i;
                break;
            }
        }

        logger.info("Player " + player.getName() + " clicked button at location: " + buttonLoc);
        logger.info("Button index: " + index);
        logger.info("Quiz button locations: " + quizButtonLocations);

        if (index == -1 || index >= currentQuestion.getAnswers().size()) {
            player.sendMessage(ChatColor.RED + "Invalid answer!");
            return;
        }

        boolean isCorrect = currentQuestion.isCorrectAnswer(index);

        if (isCorrect) {
            player.sendMessage(ChatColor.GREEN + "Correct answer!");
            dayNightManagerProvider.get().addScore(player.getUniqueId(), 1);
        } else {
            player.sendMessage(ChatColor.RED + "Incorrect answer. The correct answer was: " +
                    getCorrectAnswerLetter(currentQuestion));
        }

        startQuiz();
    }

    private String getCorrectAnswerLetter(Question question) {
        for (int i = 0; i < question.getAnswers().size(); i++) {
            if (question.isCorrectAnswer(i)) {
                return String.valueOf((char)('A' + i));
            }
        }
        return "?";
    }

    public void removeAllHolograms() {
        removeExistingHolograms(); // Remove holograms tracked by this class

        // Remove any stray holograms in the classroom
        if (classroomManager.isClassroomCreated()) {
            Location classroomLocation = classroomManager.getClassroomLocation();
            World world = classroomLocation.getWorld();
            if (world != null) {
                int width = classroomManager.getClassroomWidth();
                int length = classroomManager.getClassroomLength();
                int height = classroomManager.getClassroomHeight();

                int minX = classroomLocation.getBlockX() - (width / 2);
                int minY = classroomLocation.getBlockY() - 1;
                int minZ = classroomLocation.getBlockZ() - (length / 2);
                int maxX = minX + width;
                int maxY = minY + height;
                int maxZ = minZ + length;

                world.getEntities().stream()
                    .filter(entity -> entity instanceof ArmorStand)
                    .filter(entity -> {
                        Location loc = entity.getLocation();
                        return loc.getBlockX() >= minX && loc.getBlockX() < maxX
                            && loc.getBlockY() >= minY && loc.getBlockY() < maxY
                            && loc.getBlockZ() >= minZ && loc.getBlockZ() < maxZ;
                    })
                    .forEach(entity -> {
                        logger.info("Removing stray hologram: " + entity.getCustomName() + " at " + entity.getLocation());
                        entity.remove();
                    });
            }
        }
    }

    public void cleanupQuiz() {
        removeAllHolograms();
        currentQuestion = null;
        // Remove quiz buttons and signs
        if (classroomManager.isClassroomCreated()) {
            quizButtonLocations.forEach(location -> {
                Block block = location.getBlock();
                if (block.getType() == Material.OAK_BUTTON) {
                    logger.info("Removing quiz button at " + location);
                    block.setType(Material.AIR);
                }
                Block signBlock = location.clone().add(0, 1, 0).getBlock();
                if (signBlock.getState() instanceof Sign) {
                    logger.info("Removing quiz sign at " + signBlock.getLocation());
                    signBlock.setType(Material.AIR);
                }
            });
            quizButtonLocations.clear();
        }
    }

    public void endQuiz() {
        currentQuestion = null;
        removeExistingHolograms();
    }

    private boolean isInClassroom(Location location) {
        if (!classroomManager.isClassroomCreated()) {
            return false;
        }
        Location classroomLoc = classroomManager.getClassroomLocation();
        int width = config.getClassroomWidth();
        int length = config.getClassroomLength();
        int height = config.getClassroomHeight();

        return location.getWorld().equals(classroomLoc.getWorld()) &&
               location.getX() >= classroomLoc.getX() && location.getX() < classroomLoc.getX() + width &&
               location.getY() >= classroomLoc.getY() && location.getY() < classroomLoc.getY() + height &&
               location.getZ() >= classroomLoc.getZ() && location.getZ() < classroomLoc.getZ() + length;
    }
}