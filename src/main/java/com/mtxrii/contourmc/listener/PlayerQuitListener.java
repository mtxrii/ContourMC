package com.mtxrii.contourmc.listener;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/// On quit:
/// - Send custom quit message
@Component
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        Message quitMsg = new Message(MessagePrefix.GAME, "{} has left", playerName);
        event.quitMessage(quitMsg.getMessageComponent());
    }
}
