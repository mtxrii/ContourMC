package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

@Component
public class PlayerRespawnListener implements Listener {
    private SpawnpointService spawnpointService;

    @Inject
    public PlayerRespawnListener(SpawnpointService spawnpointService) {
        this.spawnpointService = spawnpointService;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.spawnpointService.teleportLivingEntityToRandomSpawnpoint(event.getPlayer());
    }
}
