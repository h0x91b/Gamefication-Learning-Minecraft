package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.models.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizManager {
    private final Config config;
    private final List<ArmorStand> holograms = new ArrayList<>();
    private final List<Location> quizButtonLocations;
    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();
    private Question currentQuestion;
    private final Provider<DayNightManager> dayNightManagerProvider;
    private final ClassroomManager classroomManager;

    @Inject
    public QuizManager(Config config, Provider<DayNightManager> dayNightManagerProvider, ClassroomManager classroomManager) {
        this.config = config;
        this.dayNightManagerProvider = dayNightManagerProvider;
        this.classroomManager = classroomManager;
        this.quizButtonLocations = classroomManager.getQuizButtonLocations();
        initializeQuestions();
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

        // Calculate hologram position (closer to the button wall)
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
            updateButtonSign(buttonLoc, currentQuestion.getAnswers().get(i));
        }
    }

    private void updateButtonSign(Location buttonLoc, String answerText) {
        Location signLoc = buttonLoc.clone().add(0, 1, 0);
        if (signLoc.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) signLoc.getBlock().getState();
            sign.setLine(1, String.valueOf((char)('A' + quizButtonLocations.indexOf(buttonLoc))));
            sign.setLine(2, answerText);
            sign.update();
        }
    }

    public void handleQuizAnswer(Player player, Location buttonLoc) {
        if (currentQuestion == null) {
            player.sendMessage(ChatColor.RED + "The quiz hasn't started yet!");
            return;
        }

        int index = quizButtonLocations.indexOf(buttonLoc);
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

        startQuiz(); // Move to the next question
    }

    private String getCorrectAnswerLetter(Question question) {
        for (int i = 0; i < question.getAnswers().size(); i++) {
            if (question.isCorrectAnswer(i)) {
                return String.valueOf((char)('A' + i));
            }
        }
        return "?"; // This should never happen if the question is properly formed
    }

    public void cleanupQuiz() {
        removeExistingHolograms();
        currentQuestion = null;
    }

    public void endQuiz() {
        currentQuestion = null;
        removeExistingHolograms();
    }
}