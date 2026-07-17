package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.mtxrii.contourmc.util.TextUtil;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Component
public class PlayerDeathListener implements Listener {
    private Plugin plugin;
    private SpawnpointService spawnpointService;

    @Inject
    public PlayerDeathListener(Plugin plugin, SpawnpointService spawnpointService) {
        this.plugin = plugin;
        this.spawnpointService = spawnpointService;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Message deathMessage = generateDeathMessage(victim, victim.getKiller());
        event.deathMessage(deathMessage.getMessageComponent());

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            victim.spigot().respawn();
        });
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull final PlayerRespawnEvent event) {
        this.spawnpointService.teleportLivingEntityToRandomSpawnpoint(event.getPlayer());
    }

    private static Message generateDeathMessage(Player victim, Player killer) {
        if (killer != null) {
            return new Message(MessagePrefix.GAME, "{} was killed by {}", victim.getName(), killer.getName());
        }

        Message deathMessage = new Message(MessagePrefix.GAME, "{} has died", victim.getName());

        EntityDamageEvent lastDamageCause = victim.getLastDamageCause();
        if (lastDamageCause == null) {
            return deathMessage;
        }

        if (!(lastDamageCause instanceof EntityDamageByEntityEvent damageByEntity)) {
            return deathMessage.append(new Message(MessagePrefix.BLANK, " from {}", TextUtil.formatEnumName(lastDamageCause.getCause())));
        }

        final Entity damager = damageByEntity.getDamager();
        if (!(damager instanceof Projectile projectile)) {
            return new Message(MessagePrefix.GAME, "{} was killed by a {}", victim.getName(), TextUtil.formatEnumName(damager.getType()));
        }

        final String shooterLabel = switch (projectile.getShooter()) {
            case Player shooterPlayer -> shooterPlayer.getName();
            case Entity shooterEntity -> TextUtil.formatEnumName(shooterEntity.getType());
            case null, default -> null;
        };

        deathMessage = new Message(MessagePrefix.GAME, "{} was shot", victim.getName());
        if (shooterLabel != null) {
            deathMessage = deathMessage.append(new Message(MessagePrefix.BLANK, " by {}", shooterLabel));
        }
        return deathMessage;
    }
}
