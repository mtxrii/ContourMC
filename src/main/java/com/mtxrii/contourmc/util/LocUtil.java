package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public final class LocUtil {

    /**
     * Calculates an intermediate location between a starting location and a target location given a percentage of
     * the distance between them.
     * @param start Origin location (will not be modified)
     * @param end Target location (will not be modified)
     * @param t A value between 0.0 and 1.0 representing how far along the line between the two locations to move to.
     *          (0.5 is the halfway point between the two locations.)
     * @return The new location at some point between the two given locations.
     */
    public static Location lerp(Location start, Location end, double t) {
        double x = start.getX() + (end.getX() - start.getX()) * t;
        double y = start.getY() + (end.getY() - start.getY()) * t;
        double z = start.getZ() + (end.getZ() - start.getZ()) * t;
        return new Location(start.getWorld(), x, y, z);
    }
}
