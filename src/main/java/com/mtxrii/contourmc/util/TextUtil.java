package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public final class TextUtil {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static String formatEnumName(Enum<?> enumVal) {
        return enumVal.name().toLowerCase().replace('_', ' ');
    }

    public static String formatLocation(Location location) {
        String coords = '(' +
                        String.valueOf(location.getBlockX()) +
                        ", " +
                        location.getBlockY() +
                        ", " +
                        location.getBlockZ()
                        + ')';
        String worldName = location.getWorld().getName();
        return coords + " " + worldName;
    }

    // @TODO: Make this more human readable
    public static String formatTimestamp(long timestamp) {
        return DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }
}
