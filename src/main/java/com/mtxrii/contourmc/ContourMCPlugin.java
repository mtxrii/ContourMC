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

    private ZiplineService ziplineService;
    private CombatService combatService;
    private ValkeyOptionsClientService valkeyOptionsClientService;

    public ContourMCPlugin(@NotNull final Platform platform) {
        super(platform);
        pluginClass = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.ziplineService = getPlatform().getInjector().getInstance(ZiplineService.class);
        this.combatService = getPlatform().getInjector().getInstance(CombatService.class);
        this.valkeyOptionsClientService = getPlatform().getInjector().getInstance(ValkeyOptionsClientService.class);

        log.info("Plugin config: {}", this.valkeyOptionsClientService.load().options.toString());
        
        if (this.valkeyOptionsClientService.isCombatLoggingEnabled()) {
            new CombatNotificationTask(this.combatService).runTaskTimer(this, 0L, 20L);
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
                () -> this.ziplineService.renderZiplineParticles(),
                0L,
                2L
        );
    }

    @Override
    public void onDisable() {
        if (this.valkeyOptionsClientService != null) {
            this.valkeyOptionsClientService.close();
        }
        super.onDisable();
    }
}
