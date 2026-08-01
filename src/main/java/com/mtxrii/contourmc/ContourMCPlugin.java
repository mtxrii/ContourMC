package com.mtxrii.contourmc;

import com.mtxrii.contourmc.customitem.CustomItemCooldown;
import com.sxtanna.platform.Platform;
import com.sxtanna.platform.paper.PlatformPaperPlugin;
import org.jetbrains.annotations.NotNull;

public final class ContourMCPlugin extends PlatformPaperPlugin {
    public static ContourMCPlugin pluginClass;

    public ContourMCPlugin(@NotNull final Platform platform) {
        super(platform);
        pluginClass = this;
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
    }
}
