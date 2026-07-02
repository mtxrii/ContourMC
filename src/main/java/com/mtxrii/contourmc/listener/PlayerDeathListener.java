package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

@Component
public class PlayerDeathListener implements Listener {
    private Plugin plugin;

    @Inject
    public PlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();

        event.setShowDeathMessages(false);

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            victim.spigot().respawn();
        });
    }
}
