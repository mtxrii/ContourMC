package com.mtxrii.contourmc;

import com.mtxrii.contourmc.customitem.CustomItemCooldown;
import com.mtxrii.contourmc.listener.CombatListener;
import com.mtxrii.contourmc.runnabletask.CombatNotificationTask;
import com.mtxrii.contourmc.service.CombatService;
import com.mtxrii.contourmc.service.ZiplineService;
import com.sxtanna.platform.Platform;
import com.sxtanna.platform.paper.PlatformPaperPlugin;
import org.jetbrains.annotations.NotNull;

public final class ContourMCPlugin extends PlatformPaperPlugin {
    public static ContourMCPlugin pluginClass;
    public static ZiplineService ziplineService;
    public static CombatService combatService;

    public static final boolean COMBAT_LOGGING_ENABLED = true;

    public ContourMCPlugin(@NotNull final Platform platform) {
        super(platform);
        pluginClass = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        ziplineService = getPlatform().getInjector().getInstance(ZiplineService.class);
        
        if (COMBAT_LOGGING_ENABLED) {
            combatService = new CombatService();
            getServer().getPluginManager().registerEvents(new CombatListener(combatService), this);
            new CombatNotificationTask(combatService).runTaskTimer(this, 0L, 20L);
        }

        getServer().getScheduler().runTaskTimer(
                this,
                CustomItemCooldown::cleanupExpiredCooldowns,
                20L,
                20L
        );

        // Start periodic particle rendering task for all active ziplines
        getServer().getScheduler().runTaskTimer(
                this,
                () -> ziplineService.renderZiplineParticles(),
                0L,
                2L
        );
    }
}
