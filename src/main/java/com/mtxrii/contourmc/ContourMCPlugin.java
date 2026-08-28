package com.mtxrii.contourmc;

import com.mtxrii.contourmc.customitem.CustomItemCooldown;
import com.mtxrii.contourmc.service.ZiplineService;
import com.sxtanna.platform.Platform;
import com.sxtanna.platform.paper.PlatformPaperPlugin;
import org.jetbrains.annotations.NotNull;

public final class ContourMCPlugin extends PlatformPaperPlugin {
    public static ContourMCPlugin pluginClass;
    public static ZiplineService ziplineService;

    public ContourMCPlugin(@NotNull final Platform platform) {
        super(platform);
        pluginClass = this;
        ziplineService = new ZiplineService(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();

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
