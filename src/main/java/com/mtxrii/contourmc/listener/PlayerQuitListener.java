package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.CombatService;
import com.mtxrii.contourmc.service.ValkeyOptionsClientService;
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
    private final CombatService combatService;
    private final ValkeyOptionsClientService valkeyOptionsClientService;

    @Inject
    public PlayerQuitListener(
            PlayerRegistryService playerRegistryService,
            CombatService combatService,
            ValkeyOptionsClientService valkeyOptionsClientService
    ) {
        this.playerRegistryService = playerRegistryService;
        this.combatService = combatService;
        this.valkeyOptionsClientService = valkeyOptionsClientService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (this.valkeyOptionsClientService.isCombatLoggingEnabled() && this.combatService.isInCombat(player.getUniqueId())) {
            player.setHealth(0);
        }

        this.playerRegistryService.logoutPlayer(player);

        Message quitMsg = new Message(MessagePrefix.GAME, "{} has left", playerName);
        event.quitMessage(quitMsg.getMessageComponent());
    }
}
