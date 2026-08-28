package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.config.ZiplineConfiguration;
import com.mtxrii.contourmc.exception.CommandArgumentException;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.particle.ParticleWithDataSpawn;
import com.mtxrii.contourmc.util.LocUtil;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@Component
@Singleton
public class ZiplineService {
    private final JacksonConfigurationLoader configLoader;
    private final ZiplineConfiguration ziplineConfig;
    private final Logger pluginLogger;
    private final Map<UUID, Location> pendingZiplineStarts = new HashMap<>();

    @Inject
    public ZiplineService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                                                      .path(plugin.getDataPath().resolve("ziplines.json"))
                                                      .build();
        try {
            ZiplineConfiguration loadedConfig = this.configLoader.load().get(ZiplineConfiguration.class);
            this.ziplineConfig = loadedConfig != null ? loadedConfig : new ZiplineConfiguration();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        this.pluginLogger = plugin.getLogger();
    }

    public Set<String> ziplineNames() {
        return this.ziplineConfig.ziplines.keySet();
    }

    public void setPendingStart(@NotNull UUID playerId, @NotNull Location location) {
        this.pendingZiplineStarts.put(playerId, location);
    }

    public @Nullable Location getPendingStart(@NotNull UUID playerId) {
        return this.pendingZiplineStarts.remove(playerId);
    }

    public void createZipline(@NotNull String name, @NotNull Location start, @NotNull Location end) throws CommandArgumentException {
        if (this.ziplineConfig.ziplines.containsKey(name)) {
            throw new CommandArgumentException(MessagePrefix.ZIPLINE, "A zipline with this name already exists.");
        }
        if (start.getWorld() == null || end.getWorld() == null || !start.getWorld().equals(end.getWorld())) {
            throw new CommandArgumentException(MessagePrefix.ZIPLINE, "Zipline start and end must be in the same world.");
        }

        ZiplineConfiguration.Zipline entry = new ZiplineConfiguration.Zipline();
        entry.startWorld = start.getWorld().getName();
        entry.startX = start.getX();
        entry.startY = start.getY();
        entry.startZ = start.getZ();

        entry.endWorld = end.getWorld().getName();
        entry.endX = end.getX();
        entry.endY = end.getY();
        entry.endZ = end.getZ();

        this.ziplineConfig.ziplines.put(name, entry);
        this.saveConfig();
    }

    public void deleteZipline(@NotNull String name) throws CommandArgumentException {
        if (!this.ziplineConfig.ziplines.containsKey(name)) {
            throw new CommandArgumentException(MessagePrefix.ZIPLINE, "No zipline found with this name.");
        }
        this.ziplineConfig.ziplines.remove(name);
        this.saveConfig();
    }

    public @Nullable ZiplineConfiguration.Zipline getZipline(@NotNull String name) {
        return this.ziplineConfig.ziplines.get(name);
    }

    public @Nullable ZiplineMatch findZiplineByAnchor(@NotNull Location location) {
        for (Map.Entry<String, ZiplineConfiguration.Zipline> entry : this.ziplineConfig.ziplines.entrySet()) {
            ZiplineConfiguration.Zipline z = entry.getValue();
            Location start = LocUtil.getLocation(z.startWorld, z.startX, z.startY, z.startZ);
            Location end = LocUtil.getLocation(z.endWorld, z.endX, z.endY, z.endZ);

            if (start != null && start.getWorld().equals(location.getWorld()) && start.distanceSquared(location) <= 4.0) {
                return new ZiplineMatch(entry.getKey(), start, end, true);
            }
            if (end != null && end.getWorld().equals(location.getWorld()) && end.distanceSquared(location) <= 4.0) {
                return new ZiplineMatch(entry.getKey(), end, start, false);
            }
        }
        return null;
    }

    public void renderZiplineParticles() {
        for (ZiplineConfiguration.Zipline z : this.ziplineConfig.ziplines.values()) {
            Location start = LocUtil.getLocation(z.startWorld, z.startX, z.startY, z.startZ);
            Location end = LocUtil.getLocation(z.endWorld, z.endX, z.endY, z.endZ);
            if (start == null || end == null || !start.getWorld().equals(end.getWorld())) {
                continue;
            }

            double distance = start.distance(end);
            org.bukkit.util.Vector dir = end.toVector().subtract(start.toVector()).normalize();
            for (double d = 0; d <= distance; d += 0.5) {
                Location particleLoc = start.clone().add(dir.clone().multiply(d));
                new ParticleWithDataSpawn.DustParticleSpawn(
                        start.getWorld(),
                        particleLoc,
                        ParticleWithDataSpawn.ParticleColor.BLACK,
                        0.75f
                ).spawn();
            }
        }
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(ZiplineConfiguration.class, this.ziplineConfig));
        } catch (ConfigurateException e) {
            this.pluginLogger.severe("Failed to save ziplines configuration: " + e.getMessage());
        }
    }

    public record ZiplineMatch(String name, Location from, Location to, boolean isStartToTarget) {}
}
