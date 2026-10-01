package com.mtxrii.contourmc;

import com.mtxrii.contourmc.customitem.CustomItemCooldown;
import com.mtxrii.contourmc.runnabletask.CombatNotificationTask;
import com.mtxrii.contourmc.service.CombatService;
import com.mtxrii.contourmc.service.ValkeyOptionsClientService;
import com.mtxrii.contourmc.service.ZiplineService;
import com.sxtanna.platform.Platform;
import com.sxtanna.platform.paper.PlatformPaperPlugin;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public final class ContourMCPlugin extends PlatformPaperPlugin {
    public static ContourMCPlugin pluginClass;
    public static ZiplineService ziplineService;
    public static CombatService combatService;
    public static ValkeyOptionsClientService valkeyOptionsClientService;

    public ContourMCPlugin(@NotNull final Platform platform) {
        super(platform);
        pluginClass = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        ziplineService = getPlatform().getInjector().getInstance(ZiplineService.class);
        combatService = getPlatform().getInjector().getInstance(CombatService.class);
        valkeyOptionsClientService = getPlatform().getInjector().getInstance(ValkeyOptionsClientService.class);
        
        if (valkeyOptionsClientService.isCombatLoggingEnabled()) {
            new CombatNotificationTask(combatService).runTaskTimer(this, 0L, 20L);
            log.info("Combat logging enabled. (force-complete combats)");
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

    @Override
    public void onDisable() {
        if (valkeyOptionsClientService != null) {
            valkeyOptionsClientService.close();
        }
        super.onDisable();
    }
}
