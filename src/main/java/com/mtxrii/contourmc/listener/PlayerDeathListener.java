package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

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
        Message deathMessage;
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();
        if (killer != null) {
            deathMessage = new Message(MessagePrefix.GAME, "{} was killed by {}", victim.getName(), killer.getName());

        } else {
            deathMessage = new Message(MessagePrefix.GAME, "{} has died", victim.getName());

            EntityDamageEvent lastDamageCause = victim.getLastDamageCause();
            if (lastDamageCause != null) {
                EntityDamageEvent.DamageCause cause = lastDamageCause.getCause();
                deathMessage = deathMessage.append(new Message(MessagePrefix.BLANK, " from {}", formatEnumName(cause)));

                if (lastDamageCause instanceof EntityDamageByEntityEvent damageByEntity) {
                    Entity damager = damageByEntity.getDamager();
                    deathMessage = new Message(MessagePrefix.GAME, "{} was killed by {}", victim.getName(), formatEnumName(damager.getType()));

                    if (damager instanceof Projectile projectile) {
                        Message shooterLabel = null;
                        ProjectileSource shooter = projectile.getShooter();
                        if (shooter instanceof Player shooterPlayer) {
                            shooterLabel = new Message(MessagePrefix.BLANK, " by {}", shooterPlayer.getName());
                        } else if (shooter instanceof Entity shooterEntity) {
                            shooterLabel = new Message(MessagePrefix.BLANK, " by {}", formatEnumName(shooterEntity.getType()));
                        }
                        deathMessage = new Message(MessagePrefix.GAME, "{} was shot", victim.getName());

                        if (shooterLabel != null) {
                            deathMessage = deathMessage.append(shooterLabel);
                        }
                    }
                }
            }
        }

        event.setShowDeathMessages(false);
        deathMessage.sendToAll();

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            victim.spigot().respawn();
            this.spawnpointService.teleportLivingEntityToRandomSpawnpoint(victim);
        });
    }

    private static String formatEnumName(Enum<?> enumVal) {
        return enumVal.name().toLowerCase().replace('_', ' ');
    }
}
