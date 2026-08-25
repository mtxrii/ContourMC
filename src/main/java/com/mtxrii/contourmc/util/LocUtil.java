package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public final class LocUtil {

    public static Location lerp(Location start, Location end, double t) {
        double x = start.getX() + (end.getX() - start.getX()) * t;
        double y = start.getY() + (end.getY() - start.getY()) * t;
        double z = start.getZ() + (end.getZ() - start.getZ()) * t;
        return new Location(start.getWorld(), x, y, z);
    }
}
