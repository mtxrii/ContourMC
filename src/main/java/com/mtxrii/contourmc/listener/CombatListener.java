package com.mtxrii.contourmc.listener;

import com.mtxrii.contourmc.ContourMCPlugin;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Component
public class CombatListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player victim) {
            if (ContourMCPlugin.safeZoneService.isInSafeZone(attacker.getLocation()) || ContourMCPlugin.safeZoneService.isInSafeZone(victim.getLocation())) {
                new Message(MessagePrefix.GAME, "&cYou cannot fight in a safe zone!").sendTo(attacker);
                event.setCancelled(true);
                return;
            }

            if (ContourMCPlugin.COMBAT_LOGGING_ENABLED && ContourMCPlugin.combatService != null) {
                if (!ContourMCPlugin.combatService.isInCombat(attacker.getUniqueId())) {
                    new Message(MessagePrefix.GAME, "&cYou have engaged in combat! (Don't leave now)").sendTo(attacker);
                }
                if (!ContourMCPlugin.combatService.isInCombat(victim.getUniqueId())) {
                    new Message(MessagePrefix.GAME, "&cYou have engaged in combat! (Don't leave now)").sendTo(victim);
                }

                ContourMCPlugin.combatService.tag(attacker.getUniqueId());
                ContourMCPlugin.combatService.tag(victim.getUniqueId());
            }
        }
    }
}
