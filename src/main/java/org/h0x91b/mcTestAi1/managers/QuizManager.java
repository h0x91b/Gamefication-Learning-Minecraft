package org.h0x91b.mcTestAi1.managers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
    private final List<Location> quizButtonLocations = new ArrayList<>();
    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();
    private Question currentQuestion;
    private final Provider<DayNightManager> dayNightManagerProvider;

    @Inject
    public QuizManager(Config config, Provider<DayNightManager> dayNightManagerProvider) {
        this.config = config;
        this.dayNightManagerProvider = dayNightManagerProvider;
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
        updateHologramWithCurrentQuestion();
    }

    private void updateHologramWithCurrentQuestion() {
        if (currentQuestion == null || holograms.isEmpty()) return;

        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Question:");
        lines.add(ChatColor.YELLOW + currentQuestion.getQuestion());
        lines.add("");
        for (int i = 0; i < currentQuestion.getAnswers().size(); i++) {
            lines.add(ChatColor.WHITE + String.valueOf((char)('A' + i)) + ") " + currentQuestion.getAnswers().get(i));
        }

        for (int i = 0; i < holograms.size(); i++) {
            ArmorStand hologram = holograms.get(i);
            if (hologram.isValid() && i < lines.size()) {
                hologram.setCustomName(lines.get(i));
            } else if (hologram.isValid()) {
                hologram.setCustomName("");
            }
        }
    }

    public void handleQuizAnswer(Player player, Location buttonLoc) {
        if (currentQuestion == null) {
            player.sendMessage(ChatColor.RED + "The quiz hasn't started yet!");
            return;
        }

        int index = -1;
        for (int i = 0; i < quizButtonLocations.size(); i++) {
            if (isSameLocation(buttonLoc, quizButtonLocations.get(i))) {
                index = i;
                break;
            }
        }

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
        return "?"; // This should never happen if the question is properly formed
    }

    public void endQuiz() {
        currentQuestion = null;
        for (ArmorStand hologram : holograms) {
            if (hologram.isValid()) {
                hologram.setCustomName("");
            }
        }
    }

    private boolean isSameLocation(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
               loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
}