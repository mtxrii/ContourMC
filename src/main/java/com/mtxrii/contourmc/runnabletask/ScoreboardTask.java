package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.service.ScoreboardService;
import org.bukkit.scheduler.BukkitRunnable;

public final class ScoreboardTask extends BukkitRunnable {
    private final ScoreboardService scoreboardService;

    public ScoreboardTask(ScoreboardService scoreboardService) {
        this.scoreboardService = scoreboardService;
    }

    @Override
    public void run() {
        this.scoreboardService.updateAll();
    }
}
