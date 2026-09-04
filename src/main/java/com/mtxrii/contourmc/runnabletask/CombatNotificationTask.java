package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.service.CombatService;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatNotificationTask extends BukkitRunnable {
    private final CombatService combatService;
    private final Map<UUID, BossBar> combatBossBars = new ConcurrentHashMap<>();
    private final double lengthOfCombatSeconds;

    public CombatNotificationTask(CombatService combatService) {
        this.combatService = combatService;
        this.lengthOfCombatSeconds = CombatService.COMBAT_DURATION_MS / 1000.0;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.combatService.isInCombat(player.getUniqueId())) {
                long remaining = this.combatService.getRemainingCombatTime(player.getUniqueId()) / 1000;
                
                BossBar bossBar = combatBossBars.computeIfAbsent(player.getUniqueId(), uuid -> 
                    Bukkit.createBossBar("Combat", BarColor.RED, BarStyle.SOLID)
                );
                bossBar.addPlayer(player);
                bossBar.setTitle("In Combat: " + remaining + "s");
                bossBar.setProgress((double) remaining / this.lengthOfCombatSeconds);
            } else {
                BossBar bossBar = combatBossBars.remove(player.getUniqueId());
                if (bossBar != null) {
                    bossBar.removePlayer(player);
                }
            }
        }
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        for (BossBar bossBar : combatBossBars.values()) {
            bossBar.removeAll();
        }
        combatBossBars.clear();
    }
}
