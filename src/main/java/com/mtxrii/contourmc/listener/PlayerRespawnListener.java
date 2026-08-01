package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/// On respawn:
/// - Teleport player to random spawnpoint
@Component
public class PlayerRespawnListener implements Listener {
    private SpawnpointService spawnpointService;

    @Inject
    public PlayerRespawnListener(SpawnpointService spawnpointService) {
        this.spawnpointService = spawnpointService;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Location respawnLocation = this.spawnpointService.getRandomSpawnpointLocation();
        if (respawnLocation == null) {
            return;
        }

        event.setRespawnLocation(respawnLocation);
    }
}
