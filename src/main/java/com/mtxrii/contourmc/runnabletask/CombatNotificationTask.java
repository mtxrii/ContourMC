package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.CombatService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatNotificationTask extends BukkitRunnable {
    private final CombatService combatService;

    public CombatNotificationTask(CombatService combatService) {
        this.combatService = combatService;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.combatService.isInCombat(player.getUniqueId())) {
                long remaining = this.combatService.getRemainingCombatTime(player.getUniqueId()) / 1000;
                Message message = new Message(MessagePrefix.BLANK, "&cIn Combat: " + remaining + "s (Don't leave now)");
                message.sendAsActionBar(player);
            }
        }
    }
}
