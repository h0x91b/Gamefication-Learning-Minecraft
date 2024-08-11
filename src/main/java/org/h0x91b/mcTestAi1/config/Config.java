package org.h0x91b.mcTestAi1.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.models.Question;
import org.bukkit.Sound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Config {
    private final JavaPlugin plugin;
    private YamlConfiguration questionsConfig;
    private final Logger logger;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        plugin.saveDefaultConfig();
        loadQuestionsConfig();
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

    private void loadQuestionsConfig() {
        File questionsFile = new File(plugin.getDataFolder(), "questions.yml");
        if (!questionsFile.exists()) {
            plugin.saveResource("questions.yml", false);
        }
        questionsConfig = YamlConfiguration.loadConfiguration(questionsFile);
        logger.info("Questions configuration file loaded successfully.");
    }

    public List<Question> getQuestions(String language) {
        List<Question> questions = new ArrayList<>();
        List<?> questionsList = questionsConfig.getList(language);
        if (questionsList == null) {
            logger.warning("No questions found for language: " + language);
            return questions;
        }

        logger.info("Found " + questionsList.size() + " question entries for language: " + language);

        for (Object obj : questionsList) {
            if (obj instanceof Map) {
                try {
                    Map<?, ?> questionMap = (Map<?, ?>) obj;
                    String questionText = (String) questionMap.get("question");
                    List<String> answers = (List<String>) questionMap.get("answers");
                    int correctAnswerIndex = (int) questionMap.get("correctAnswerIndex");

                    if (questionText == null || answers == null || answers.size() != 4) {
                        logger.warning("Invalid question format: " + questionMap);
                        continue;
                    }

                    questions.add(new Question(questionText, answers, correctAnswerIndex));
                } catch (ClassCastException | NullPointerException e) {
                    logger.warning("Error parsing question: " + e.getMessage());
                }
            } else {
                logger.warning("Invalid question entry: " + obj);
            }
        }

        logger.info("Successfully loaded " + questions.size() + " valid questions for language: " + language);
        return questions;
    }

    public Sound getCorrectAnswerSound() {
        String soundName = plugin.getConfig().getString("quiz.sounds.correct", Sound.ENTITY_PLAYER_LEVELUP.name());
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid correct answer sound in config: " + soundName + ". Using default.");
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
    }

    public Sound getIncorrectAnswerSound() {
        String soundName = plugin.getConfig().getString("quiz.sounds.incorrect", Sound.ENTITY_VILLAGER_NO.name());
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid incorrect answer sound in config: " + soundName + ". Using default.");
            return Sound.ENTITY_VILLAGER_NO;
        }
    }
}