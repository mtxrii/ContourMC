package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/// On join:
/// - Send custom join message
/// - Teleport player to random spawnpoint
@Component
public class PlayerJoinListener implements Listener {
    private SpawnpointService spawnpointService;

    @Inject
    public PlayerJoinListener(SpawnpointService spawnpointService) {
        this.spawnpointService = spawnpointService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        Message joinMsg = new Message(MessagePrefix.GAME, "{} has joined", playerName);
        event.joinMessage(joinMsg.getMessageComponent());

        this.spawnpointService.teleportLivingEntityToRandomSpawnpoint(event.getPlayer());
    }
}
