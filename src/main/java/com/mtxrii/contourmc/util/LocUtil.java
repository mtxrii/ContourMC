package com.mtxrii.contourmc.util;

import com.mtxrii.contourmc.config.SpawnpointsConfiguration;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Checks if this location is a block's location. Block locations are exact coordinates.
     * @param location Location to check
     * @return true if this location is an exact coordinate, false otherwise
     */
    public static boolean isBlockLocation(Location location) {
        boolean isXAnEdge = location.getX() == location.getBlockX();
        boolean isYAnEdge = location.getY() == location.getBlockY();
        boolean isZAnEdge = location.getZ() == location.getBlockZ();
        return isXAnEdge && isYAnEdge && isZAnEdge;
    }

    /**
     * Converts a Bukkit Location into a Spawnpoint configuration object.
     * @param location Location to convert
     * @return Spawnpoint configuration representation of the location
     */
    public static SpawnpointsConfiguration.Spawnpoint toSpawnpoint(@NotNull Location location) {
        SpawnpointsConfiguration.Spawnpoint spawnpoint = new SpawnpointsConfiguration.Spawnpoint();
        spawnpoint.world = location.getWorld().getName();
        spawnpoint.x = location.getX();
        spawnpoint.y = location.getY();
        spawnpoint.z = location.getZ();
        spawnpoint.yaw = location.getYaw();
        spawnpoint.pitch = location.getPitch();
        return spawnpoint;
    }

    /**
     * Converts a Spawnpoint configuration object into a Bukkit Location.
     * @param spawnpoint Spawnpoint configuration object to convert
     * @return Bukkit Location, or null if the world is not loaded
     */
    @Nullable
    public static Location toLocation(@NotNull SpawnpointsConfiguration.Spawnpoint spawnpoint) {
        World world = Bukkit.getWorld(spawnpoint.world);
        if (world == null) {
            return null;
        }
        return new Location(
                world,
                spawnpoint.x,
                spawnpoint.y,
                spawnpoint.z,
                spawnpoint.yaw,
                spawnpoint.pitch
        );
    }
}
