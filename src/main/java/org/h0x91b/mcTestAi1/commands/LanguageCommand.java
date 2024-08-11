package org.h0x91b.mcTestAi1.commands;

import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.managers.QuizManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;

import java.util.Arrays;
import java.util.List;

public class LanguageCommand implements CommandExecutor {
    private final QuizManager quizManager;
    private final DayNightManager dayNightManager;
    private static final List<String> VALID_LANGUAGES = Arrays.asList("russian", "hebrew", "english");

    @Inject
    public LanguageCommand(QuizManager quizManager, DayNightManager dayNightManager) {
        this.quizManager = quizManager;
        this.dayNightManager = dayNightManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /language <russian|hebrew|english>");
            return false;
        }

        String language = args[0].toLowerCase();
        if (!VALID_LANGUAGES.contains(language)) {
            player.sendMessage(ChatColor.RED + "Invalid language. Please choose russian, hebrew, or english.");
            return false;
        }

        try {
            quizManager.setLanguage(language);
            if (dayNightManager.isNight()) {
                player.sendMessage(ChatColor.GREEN + "Quiz language has been set to " + language + " and the quiz has been restarted.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Quiz language has been set to " + language + " for the next night.");
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while changing the language. Please try again.");
            e.printStackTrace();
        }

        return true;
    }
}