package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.PlayerRegistryConfiguration;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.time.Instant;
import java.util.UUID;

@Component
public class PlayerRegistryService {
    private final JacksonConfigurationLoader configLoader;
    private final PlayerRegistryConfiguration playerRegistryConfig;

    @Inject
    public PlayerRegistryService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                .path(plugin.getDataPath().resolve("playerRegistry.json"))
                .build();
        try {
            this.playerRegistryConfig = this.configLoader.load().get(PlayerRegistryConfiguration.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public void loginPlayer(@NotNull Player player) {
        var playerData = this.getPlayerDataById(player.getUniqueId());

        // New player
        if (playerData == null) {
            var newPlayerData = new PlayerRegistryConfiguration.PlayerData();
            newPlayerData.uniqueId = player.getUniqueId();
            newPlayerData.lastOnline = Instant.now();
            this.playerRegistryConfig.playerRegistry.put(player.getName(), newPlayerData);
            this.saveConfig();
            return;
        }

        // Existing player, new name
        if (!this.playerRegistryConfig.playerRegistry.get(player.getName()).uniqueId.equals(player.getUniqueId())) {
            this.playerRegistryConfig.playerRegistry.remove(player.getName());
            this.playerRegistryConfig.playerRegistry.put(player.getName(), playerData);
            this.saveConfig();
        }

        // Do nothing for existing player with same name
    }

    public void logoutPlayer(@NotNull Player player) {
        var playerData = this.getPlayerDataById(player.getUniqueId());
        assert playerData != null;

        playerData.lastOnline = Instant.now();
        this.saveConfig();
    }

    private PlayerRegistryConfiguration.PlayerData getPlayerDataById(UUID playerId) {
        for (var playerData : this.playerRegistryConfig.playerRegistry.values()) {
            if (playerData.uniqueId.equals(playerId)) {
                return playerData;
            }
        }
        return null;
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.playerRegistryConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
