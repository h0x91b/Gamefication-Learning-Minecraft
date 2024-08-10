package org.h0x91b.mcTestAi1.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.models.Question;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {
    private final JavaPlugin plugin;
    private YamlConfiguration questionsConfig;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
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
    }

    public List<Question> getQuestions(String language) {
        List<Question> questions = new ArrayList<>();
        List<?> questionsList = questionsConfig.getList(language);
        if (questionsList == null) {
            plugin.getLogger().warning("No questions found for language: " + language);
            return questions;
        }

        for (Object obj : questionsList) {
            if (obj instanceof Map) {
                Map<?, ?> questionMap = (Map<?, ?>) obj;
                String questionText = (String) questionMap.get("question");
                List<String> answers = (List<String>) questionMap.get("answers");
                int correctAnswerIndex = (int) questionMap.get("correctAnswerIndex");
                questions.add(new Question(questionText, answers, correctAnswerIndex));
            }
        }
        return questions;
    }
}