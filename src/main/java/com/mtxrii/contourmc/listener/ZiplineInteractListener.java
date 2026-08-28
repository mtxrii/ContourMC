package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.ContourMCPlugin;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.runnabletask.ZiplineTask;
import com.mtxrii.contourmc.service.ZiplineService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@Component
public class ZiplineInteractListener implements Listener {
    private final ZiplineService ziplineService;

    @Inject
    public ZiplineInteractListener(ZiplineService ziplineService) {
        this.ziplineService = ziplineService;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Player player = event.getPlayer();
        ZiplineService.ZiplineMatch match = this.ziplineService.findZiplineByAnchor(clickedBlock.getLocation());
        if (match == null) {
            return;
        }

        if (player.isSneaking()) {
            return;
        }

        event.setCancelled(true);
        new Message(MessagePrefix.ZIPLINE, "Riding '{}'...", match.name()).sendAsActionBar(player);
        new ZiplineTask(player, match.from(), match.to(), 1.5).runTaskTimer(ContourMCPlugin.pluginClass, 0L, 1L);
    }
}
