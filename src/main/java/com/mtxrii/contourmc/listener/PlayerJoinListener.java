package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/// On join:
/// - Send custom join message
/// - Teleport player to random spawnpoint
/// - If player has no rank, set their rank to default
@Component
public class PlayerJoinListener implements Listener {
    private final SpawnpointService spawnpointService;
    private final RankService rankService;
    private final PlayerRegistryService playerRegistryService;

    @Inject
    public PlayerJoinListener(
            SpawnpointService spawnpointService,
            RankService rankService,
            PlayerRegistryService playerRegistryService
    ) {
        this.spawnpointService = spawnpointService;
        this.rankService = rankService;
        this.playerRegistryService = playerRegistryService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        this.playerRegistryService.loginPlayer(player);

        Message joinMsg = new Message(MessagePrefix.GAME, "{} has joined", playerName);
        event.joinMessage(joinMsg.getMessageComponent());

        this.spawnpointService.teleportLivingEntityToRandomSpawnpoint(event.getPlayer());

        if (this.rankService.getRank(player.getUniqueId()) == null) {
            this.rankService.setRank(player.getUniqueId(), Rank.PLAYER);
        }
    }
}
