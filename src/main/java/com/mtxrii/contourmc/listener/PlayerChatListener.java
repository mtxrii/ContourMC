package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.SanctionService;
import com.sxtanna.platform.archetype.Component;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Logger;

@Component
public class PlayerChatListener implements Listener {
    private final SanctionService sanctionService;
    private final Logger pluginLogger;

    @Inject
    public PlayerChatListener(
            SanctionService sanctionService,
            Plugin plugin
    ) {
        this.sanctionService = sanctionService;
        this.pluginLogger = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        SanctionsConfiguration.Sanction muteInfo = this.sanctionService.getMute(playerId);
        String eventMessage = ((TextComponent) event.message()).content();

        if (muteInfo != null) {
            new Message(
                    MessagePrefix.GAME,
                    "You cannot chat while muted!\nReason: {}\nUntil: {}",
                    muteInfo.reason,
                    muteInfo.expiresAt
            ).sendTo(player);
            event.setCancelled(true);

            this.pluginLogger.info(
                    "Player " + player.getName() + " tried to send this message while muted: " + eventMessage
            );
        }
    }
}
