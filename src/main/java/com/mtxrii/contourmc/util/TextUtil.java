package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public final class TextUtil {

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
}
