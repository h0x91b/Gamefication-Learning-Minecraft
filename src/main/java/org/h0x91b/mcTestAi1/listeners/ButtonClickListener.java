package org.h0x91b.mcTestAi1.listeners;

import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.h0x91b.mcTestAi1.managers.QuizManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;

public class ButtonClickListener implements Listener {
    private final QuizManager quizManager;
    private final DayNightManager dayNightManager;

    @Inject
    public ButtonClickListener(QuizManager quizManager, DayNightManager dayNightManager) {
        this.quizManager = quizManager;
        this.dayNightManager = dayNightManager;
    }

    @EventHandler
    public void onButtonClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.OAK_BUTTON) return;
        if (!dayNightManager.isNight()) return; // Only allow quiz interactions during night time

        event.setCancelled(true); // Prevent default button behavior
        quizManager.handleQuizAnswer(event.getPlayer(), event.getClickedBlock().getLocation());
    }
}