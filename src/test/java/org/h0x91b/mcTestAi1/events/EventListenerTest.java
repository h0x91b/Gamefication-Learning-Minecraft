package org.h0x91b.mcTestAi1.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.h0x91b.mcTestAi1.config.Config;
import org.h0x91b.mcTestAi1.managers.ClassroomManager;
import org.h0x91b.mcTestAi1.managers.DayNightManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventListenerTest {

    @Mock
    private ClassroomManager classroomManager;
    @Mock
    private DayNightManager dayNightManager;
    @Mock
    private JavaPlugin plugin;
    @Mock
    private Config config;
    @Mock
    private Player attacker;
    @Mock
    private Player victim;
    @Mock
    private Location location;
    @Mock
    private EntityDamageByEntityEvent event;
    @Mock
    private Logger logger;

    private EventListener eventListener;

    @Before
    public void setUp() {
        when(plugin.getLogger()).thenReturn(logger);
        eventListener = new EventListener(classroomManager, dayNightManager, plugin, config);
    }

    @Test
    public void testPvPDamageInClassroom() {
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(victim.getLocation()).thenReturn(location);
        when(classroomManager.isClassroomBlock(location)).thenReturn(true);

        eventListener.onEntityDamageByEntity(event);

        verify(event).setCancelled(true);
        verify(attacker).sendMessage("ПвП запрещено в классной комнате!");
        verify(logger).info(contains("Prevented PvP damage in classroom"));
    }

    @Test
    public void testPvPDamageOutsideClassroom() {
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(victim.getLocation()).thenReturn(location);
        when(classroomManager.isClassroomBlock(location)).thenReturn(false);

        eventListener.onEntityDamageByEntity(event);

        verify(event, never()).setCancelled(true);
        verify(attacker, never()).sendMessage(anyString());
    }
}