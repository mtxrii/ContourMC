package com.mtxrii.contourmc;

import com.mtxrii.contourmc.customitem.CustomItem;
import com.sxtanna.platform.Platform;
import com.sxtanna.platform.paper.PlatformPaperPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ContourMCPlugin extends PlatformPaperPlugin {

    public static final Map<String, Map<CustomItem, Date>> CUSTOM_ITEM_USAGE_COOLDOWN_MAP = new HashMap();
    private static final long CUSTOM_ITEM_USAGE_COOLDOWN_EXPIRY_MILLIS = TimeUnit.MINUTES.toMillis(1);

    public ContourMCPlugin(@NotNull final Platform platform) {
        super(platform);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        getServer().getScheduler().runTaskTimer(
                this,
                this::cleanupExpiredCooldowns,
                20L,
                20L
        );
    }

    private void cleanupExpiredCooldowns() {
        final long now = System.currentTimeMillis();

        CUSTOM_ITEM_USAGE_COOLDOWN_MAP.values()
                                      .forEach(
                                              map -> map.entrySet()
                                                        .removeIf(
                                                                entry -> now - entry.getValue().getTime() > CUSTOM_ITEM_USAGE_COOLDOWN_EXPIRY_MILLIS
                                                        )
                                      );
    }

}
