package com.mtxrii.contourmc.service;
import com.google.inject.Inject;
import com.mtxrii.contourmc.config.SpawnpointsConfiguration;
import com.mtxrii.contourmc.exception.CommandArgumentException;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@Component
public class SpawnpointService {
    private final JacksonConfigurationLoader configLoader;
    private final SpawnpointsConfiguration spawnpointsConfig;
    private final Logger pluginLogger;

    @Inject
    public SpawnpointService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                .path(plugin.getDataPath().resolve("spawnpoints.json"))
                .build();
        try {
            this.spawnpointsConfig = this.configLoader.load().get(SpawnpointsConfiguration.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        this.pluginLogger = plugin.getLogger();
    }

    public Set<String> spawnpoints() {
        return this.spawnpointsConfig.spawnpoints.keySet();
    }

    public void saveNewSpawnpoint(
            @NotNull String spawnpointName,
            @NotNull Location spawnpointLocation
    ) throws CommandArgumentException {
        if (this.spawnpointsConfig.spawnpoints.containsKey(spawnpointName)) {
            throw new CommandArgumentException(
                    MessagePrefix.SPAWN,
                    "Spawn point already exists with this name. Either delete it first or choose a different name."
            );
        }

        SpawnpointsConfiguration.Spawnpoint spawnpoint = new SpawnpointsConfiguration.Spawnpoint();
        spawnpoint.world = spawnpointLocation.getWorld().getName();
        spawnpoint.x = spawnpointLocation.getX();
        spawnpoint.y = spawnpointLocation.getY();
        spawnpoint.z = spawnpointLocation.getZ();
        spawnpoint.yaw = spawnpointLocation.getYaw();
        spawnpoint.pitch = spawnpointLocation.getPitch();

        this.spawnpointsConfig.spawnpoints.put(spawnpointName, spawnpoint);
        this.saveConfig();
    }

    public void deleteSpawnpoint(
            @NotNull String spawnpointName
    ) throws CommandArgumentException {
        if (!this.spawnpointsConfig.spawnpoints.containsKey(spawnpointName)) {
            throw new CommandArgumentException(MessagePrefix.SPAWN, "No spawn point exists with this name.");
        }

        this.spawnpointsConfig.spawnpoints.remove(spawnpointName);
        this.saveConfig();
    }

    public void teleportLivingEntityToSpawnpoint(
            @NotNull String spawnpointName,
            @NonNull LivingEntity entity
    ) throws CommandArgumentException {
        if (!this.spawnpointsConfig.spawnpoints.containsKey(spawnpointName)) {
            throw new CommandArgumentException(MessagePrefix.SPAWN, "No spawn point exists with this name.");
        }

        SpawnpointsConfiguration.Spawnpoint spawnpoint = this.spawnpointsConfig.spawnpoints.get(spawnpointName);
        World world = Bukkit.getWorld(spawnpoint.world);
        if (world == null) {
            throw new CommandArgumentException(MessagePrefix.SPAWN, "The world for this spawn point is not loaded.");
        }

        Location spawnpointLocation = new Location(
                world,
                spawnpoint.x,
                spawnpoint.y,
                spawnpoint.z,
                spawnpoint.yaw,
                spawnpoint.pitch
        );
        entity.teleport(spawnpointLocation);
    }

    public void teleportLivingEntityToRandomSpawnpoint(
            @NonNull LivingEntity entity
    ) {
        final Set<String> spawnpointNames = this.spawnpoints();
        if (spawnpointNames.isEmpty()) {
            this.pluginLogger.warning("Failed to teleport " + entity.getName() + " to a random spawnpoint as there are none saved.");
            return;
        }

        final ArrayList<String> shuffled = new ArrayList<>(spawnpointNames);
        Collections.shuffle(shuffled);
        String spawnpointName = shuffled.get(ThreadLocalRandom.current().nextInt(spawnpointNames.size()));

        this.teleportLivingEntityToSpawnpoint(spawnpointName, entity);
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.spawnpointsConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
