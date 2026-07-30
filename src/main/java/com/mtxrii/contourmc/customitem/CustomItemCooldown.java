package com.mtxrii.contourmc.customitem;

import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class CustomItemCooldown {
    public static final Map<String, Map<CustomItem, Date>> COOLDOWN_MAP = new HashMap();
    private static final long COOLDOWN_EXPIRY_MILLIS = TimeUnit.MINUTES.toMillis(1);

    public static void cleanupExpiredCooldowns() {
        final long now = System.currentTimeMillis();

        COOLDOWN_MAP.values().forEach(map -> map.entrySet().removeIf(entry -> now - entry.getValue().getTime() > COOLDOWN_EXPIRY_MILLIS));
    }
}
