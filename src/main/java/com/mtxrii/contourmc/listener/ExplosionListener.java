package com.mtxrii.contourmc.listener;

import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

@Component
public class ExplosionListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed) {
            // Prevent any blocks from breaking
            event.blockList().clear();
        }
    }
}
