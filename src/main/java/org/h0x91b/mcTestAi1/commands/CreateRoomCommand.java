package org.h0x91b.mcTestAi1.commands;

import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;

public class CreateRoomCommand implements CommandExecutor {
    private final ClassroomManager classroomManager;

    @Inject
    public CreateRoomCommand(ClassroomManager classroomManager) {
        this.classroomManager = classroomManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return false;
        }
        
        Player player = (Player) sender;
        if (!player.hasPermission("mctestai1.createroom")) {
            player.sendMessage("You don't have permission to create a classroom!");
            return false;
        }

        try {
            classroomManager.createRoom(player);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            player.sendMessage("Error creating classroom: " + e.getMessage());
            return false;
        }
    }
}