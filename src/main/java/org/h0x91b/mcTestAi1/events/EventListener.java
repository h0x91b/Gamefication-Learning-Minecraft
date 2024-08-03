package org.h0x91b.mcTestAi1.events;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;

public class EventListener implements Listener {
    private final ClassroomManager classroomManager;

    @Inject
    public EventListener(ClassroomManager classroomManager) {
        this.classroomManager = classroomManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (classroomManager.isClassroomBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Эй, братан! Эту комнату нельзя ломать!");
        }
    }
}