package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.ContourMCPlugin;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.CombatService;
import com.mtxrii.contourmc.service.SafeZoneService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Component
public class CombatListener implements Listener {
    private final CombatService combatService;
    private final SafeZoneService safeZoneService;

    @Inject
    public CombatListener(CombatService combatService, SafeZoneService safeZoneService) {
        this.combatService = combatService;
        this.safeZoneService = safeZoneService;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (this.safeZoneService.isInSafeZone(attacker.getLocation()) || this.safeZoneService.isInSafeZone(victim.getLocation())) {
            new Message(MessagePrefix.GAME, "&cYou cannot fight in a safe zone!").sendTo(attacker);
            event.setCancelled(true);
            return;
        }

        this.tagPlayersInCombat(attacker, victim);
    }

    private void tagPlayersInCombat(Player attacker, Player victim) {
        if (ContourMCPlugin.COMBAT_LOGGING_ENABLED) {
            if (!this.combatService.isInCombat(attacker.getUniqueId())) {
                new Message(MessagePrefix.GAME, "&cYou have engaged in combat! (Don't leave now)").sendTo(attacker);
            }
            if (!this.combatService.isInCombat(victim.getUniqueId())) {
                new Message(MessagePrefix.GAME, "&cYou have engaged in combat! (Don't leave now)").sendTo(victim);
            }

            this.combatService.tag(attacker.getUniqueId());
            this.combatService.tag(victim.getUniqueId());
        }
    }
}
