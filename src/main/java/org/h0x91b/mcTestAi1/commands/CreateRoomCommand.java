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
        if (sender instanceof Player) {
            classroomManager.createRoom((Player) sender);
            return true;
        } else {
            sender.sendMessage("Эта команда только для игроков, чувак!");
            return false;
        }
    }
}