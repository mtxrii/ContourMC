package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.ContourMCPlugin;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/// On quit:
/// - Send custom quit message
@Component
public class PlayerQuitListener implements Listener {
    private final PlayerRegistryService playerRegistryService;

    @Inject
    public PlayerQuitListener(
            PlayerRegistryService playerRegistryService
    ) {
        this.playerRegistryService = playerRegistryService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (ContourMCPlugin.combatService.isInCombat(player.getUniqueId())) {
            player.setHealth(0);
        }

        this.playerRegistryService.logoutPlayer(player);

        Message quitMsg = new Message(MessagePrefix.GAME, "{} has left", playerName);
        event.quitMessage(quitMsg.getMessageComponent());
    }
}
