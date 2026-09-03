package com.mtxrii.contourmc.listener;

import com.mtxrii.contourmc.service.CombatService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final CombatService combatService;

    public CombatListener(CombatService combatService) {
        this.combatService = combatService;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player victim) {
            this.combatService.tag(attacker.getUniqueId());
            this.combatService.tag(victim.getUniqueId());
        }
    }
}
