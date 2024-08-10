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
import org.bukkit.scheduler.BukkitRunnable;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.models.Question;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class QuizManager {
    private final Config config;
    private final List<ArmorStand> holograms = new ArrayList<>();
    private List<Location> quizButtonLocations;
    private final Map<String, List<Question>> questions = new HashMap<>();
    private final Random random = new Random();
    private Question currentQuestion;
    private final Provider<DayNightManager> dayNightManagerProvider;
    private final ClassroomManager classroomManager;
    private final Logger logger;
    private final JavaPlugin plugin;
    private final Map<Location, Boolean> buttonStates = new ConcurrentHashMap<>();
    private List<Question> unusedQuestions;
    private String currentLanguage = "russian";
    private List<Question> usedQuestions;
    private final int RECENT_QUESTIONS_TO_AVOID = 5;
    private LinkedList<Question> recentlyUsedQuestions = new LinkedList<>();

    @Inject
    public QuizManager(JavaPlugin plugin, Config config, Provider<DayNightManager> dayNightManagerProvider, ClassroomManager classroomManager) {
        this.plugin = plugin;
        this.config = config;
        this.dayNightManagerProvider = dayNightManagerProvider;
        this.classroomManager = classroomManager;
        this.logger = plugin.getLogger();
        this.unusedQuestions = new ArrayList<>();
        initializeQuestions();
        updateQuizButtonLocations();
        this.usedQuestions = new ArrayList<>();
        resetQuestionPool();
    }

    private void updateQuizButtonLocations() {
        this.quizButtonLocations = classroomManager.getQuizButtonLocations();
        logger.info("Updated quiz button locations: " + quizButtonLocations);
    }

    private void initializeQuestions() {
        List<String> supportedLanguages = List.of("english", "russian", "hebrew");
        for (String language : supportedLanguages) {
            List<Question> loadedQuestions = config.getQuestions(language);
            questions.put(language, loadedQuestions);
            logger.info("Loaded " + loadedQuestions.size() + " questions for language: " + language);
        }
        if (questions.values().stream().allMatch(List::isEmpty)) {
            logger.severe("No questions loaded for any language. Please check your questions.yml file.");
        }
    }

    public void resetQuestionPool() {
        List<Question> allQuestions = new ArrayList<>(questions.get(currentLanguage));
        if (allQuestions.isEmpty()) {
            logger.severe("No questions available for language: " + currentLanguage);
            return;
        }
        unusedQuestions = new ArrayList<>(allQuestions);
        usedQuestions.clear();
        recentlyUsedQuestions.clear();
        Collections.shuffle(unusedQuestions, random);
        logger.info("Question pool has been reset and shuffled. Total questions: " + unusedQuestions.size());
    }

    public void startQuiz() {
        if (questions.get(currentLanguage).isEmpty()) {
            throw new IllegalStateException("No questions available for the quiz in the current language!");
        }
        removeAllHolograms();
        updateQuizButtonLocations();

        if (unusedQuestions.isEmpty()) {
            resetQuestionPool();
        }

        Question selectedQuestion = null;
        for (Question question : unusedQuestions) {
            if (!recentlyUsedQuestions.contains(question)) {
                selectedQuestion = question;
                break;
            }
        }

        if (selectedQuestion == null) {
            selectedQuestion = unusedQuestions.get(0);
            logger.info("All questions have been recently used. Selecting the least recently used question.");
        }

        currentQuestion = selectedQuestion;
        unusedQuestions.remove(currentQuestion);
        usedQuestions.add(currentQuestion);

        recentlyUsedQuestions.addFirst(currentQuestion);
        if (recentlyUsedQuestions.size() > RECENT_QUESTIONS_TO_AVOID) {
            recentlyUsedQuestions.removeLast();
        }

        resetAllButtonStates();
        updateHologramWithCurrentQuestion();
        logger.info("New quiz question started: " + currentQuestion.getQuestion());
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

        Location hologramLocation = classroomLocation.clone().add(0, 1, -4);

        createHologramLine(world, hologramLocation, ChatColor.GOLD + "Вопрос:");
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
        if (currentQuestion == null || !isButtonEnabled(buttonLoc)) {
            player.sendMessage(ChatColor.RED + "Эта кнопка неактивна!");
            return;
        }

        int index = getButtonIndex(buttonLoc);

        if (index == -1 || index >= currentQuestion.getAnswers().size()) {
            player.sendMessage(ChatColor.RED + "Неверный ответ!");
            return;
        }

        boolean isCorrect = currentQuestion.isCorrectAnswer(index);

        for (Location loc : quizButtonLocations) {
            disableButton(loc);
            updateButtonAppearance(loc, false);
        }

        if (isCorrect) {
            int additionalTime = dayNightManagerProvider.get().addScore(player.getUniqueId(), 1);
            player.sendMessage(ChatColor.GREEN + "Правильно! Твой следующий день будет на " + additionalTime + " " + getSecondsWord(additionalTime) + " длиннее!");
            scheduleButtonReEnable(1);
        } else {
            String correctAnswer = getCorrectAnswerWithLetter(currentQuestion);
            player.sendMessage(ChatColor.RED + "Неправильно. Правильный ответ был: " + correctAnswer);
            scheduleButtonReEnable(3);
        }

        logger.info("Answer processed. Correct: " + isCorrect + ". Buttons will be re-enabled shortly.");
    }

    private void scheduleButtonReEnable(int seconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                logger.info("Re-enabling buttons after " + seconds + " seconds");
                resetAllButtonStates();
                startQuiz();
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    private boolean isButtonEnabled(Location buttonLoc) {
        return buttonStates.getOrDefault(buttonLoc, true);
    }

    private void disableButton(Location buttonLoc) {
        buttonStates.put(buttonLoc, false);
    }

    private void enableButton(Location buttonLoc) {
        buttonStates.put(buttonLoc, true);
    }

    private void updateButtonAppearance(Location buttonLoc, boolean enabled) {
        Block block = buttonLoc.getBlock();
        block.setType(enabled ? Material.OAK_BUTTON : Material.AIR);
        Block signBlock = buttonLoc.clone().add(0, 1, 0).getBlock();
        if (signBlock.getState() instanceof Sign) {
            Sign sign = (Sign) signBlock.getState();
            if (enabled && currentQuestion != null) {
                int index = getButtonIndex(buttonLoc);
                if (index >= 0 && index < currentQuestion.getAnswers().size()) {
                    sign.setLine(0, ChatColor.BOLD + String.valueOf((char)('A' + index)));
                    sign.setLine(1, currentQuestion.getAnswers().get(index));
                } else {
                    sign.setLine(0, "");
                    sign.setLine(1, "");
                }
            } else {
                sign.setLine(0, "");
                sign.setLine(1, "");
            }
            sign.update(true);
        }
    }

    private boolean allButtonsDisabled() {
        return buttonStates.values().stream().noneMatch(enabled -> enabled);
    }

    private String getCorrectAnswerWithLetter(Question question) {
        for (int i = 0; i < question.getAnswers().size(); i++) {
            if (question.isCorrectAnswer(i)) {
                char letter = (char)('A' + i);
                return letter + " - " + question.getAnswers().get(i);
            }
        }
        return "Ответ не найден";
    }

    private String getSecondsWord(int seconds) {
        if (seconds % 10 == 1 && seconds % 100 != 11) {
            return "секунду";
        } else if ((seconds % 10 == 2 || seconds % 10 == 3 || seconds % 10 == 4) &&
                (seconds % 100 < 10 || seconds % 100 >= 20)) {
            return "секунды";
        } else {
            return "секунд";
        }
    }

    private int getButtonIndex(Location buttonLoc) {
        for (int i = 0; i < quizButtonLocations.size(); i++) {
            Location loc = quizButtonLocations.get(i);
            if (buttonLoc.getBlockX() == loc.getBlockX() &&
                buttonLoc.getBlockY() == loc.getBlockY() &&
                buttonLoc.getBlockZ() == loc.getBlockZ()) {
                return i;
            }
        }
        return -1;
    }

    public void removeAllHolograms() {
        removeExistingHolograms();

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

    private void resetAllButtonStates() {
        for (Location buttonLoc : quizButtonLocations) {
            try {
                enableButton(buttonLoc);
                updateButtonAppearance(buttonLoc, true);
            } catch (Exception e) {
                logger.warning("Faile d to reset button state for location: " + buttonLoc + ". Error: " + e.getMessage());
            }
        
        }
        logger.info("All button states reset");
    }

    public void cleanupQuiz() {
        logger.info("Resetting quiz state...");
        removeAllHolograms();
        resetAllButtonStates();
        currentQuestion = null;
        logger.info("Quiz state reset completed.");
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

    public void setLanguage(String language) {
        if (questions.containsKey(language)) {
            this.currentLanguage = language;
            unusedQuestions = null;
            logger.info("Quiz language changed to: " + language);
        } else {
            logger.warning("Attempted to set unsupported language: " + language);
        }
    }
}