package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SanctionService;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/// On connect:
/// - If player is banned, disallow join
///
/// On join:
/// - Send custom join message
/// - Teleport player to random spawnpoint
/// - If player has no rank, set their rank to default
@Component
public class PlayerJoinListener implements Listener {
    private final SpawnpointService spawnpointService;
    private final RankService rankService;
    private final PlayerRegistryService playerRegistryService;
    private final SanctionService sanctionService;

    @Inject
    public PlayerJoinListener(
            SpawnpointService spawnpointService,
            RankService rankService,
            PlayerRegistryService playerRegistryService,
            SanctionService sanctionService
    ) {
        this.spawnpointService = spawnpointService;
        this.rankService = rankService;
        this.playerRegistryService = playerRegistryService;
        this.sanctionService = sanctionService;
    }

    @EventHandler
    public void onPlayerConnect(AsyncPlayerPreLoginEvent event) {
        UUID playerId = event.getUniqueId();
        SanctionsConfiguration.Sanction banInfo = this.sanctionService.getBan(playerId);

        if (banInfo != null) {
            Message disallowJoinMessage = this.sanctionService.getBanMessage(playerId, banInfo.reason, banInfo.expiresAt);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, disallowJoinMessage.getMessageComponent());
        }
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
