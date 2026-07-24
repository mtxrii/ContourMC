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
import java.util.logging.Logger;

@Component
public class PlayerRegistryService {
    private final JacksonConfigurationLoader configLoader;
    private final PlayerRegistryConfiguration playerRegistryConfig;
    private final Logger pluginLogger;

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
        this.pluginLogger = plugin.getLogger();
    }

    public void loginPlayer(@NotNull Player player) {
        var playerData = this.getPlayerDataById(player.getUniqueId());
        String currentPlayerName = player.getName();

        // New player
        if (playerData == null) {
            this.pluginLogger.info("New player joined: " + currentPlayerName + " (" + player.getUniqueId() + ")");
            var newPlayerData = new PlayerRegistryConfiguration.PlayerData();
            newPlayerData.uniqueId = player.getUniqueId();
            newPlayerData.name = currentPlayerName;
            newPlayerData.firstOnline = Instant.now().toString();
            newPlayerData.lastOnline = Instant.now().toString();
            this.playerRegistryConfig.playerRegistry.put(currentPlayerName, newPlayerData);
            this.saveConfig();
            return;
        }

        // Existing player, new name
        String oldPlayerName = playerData.name;
        if (!oldPlayerName.equals(currentPlayerName)) {
            this.pluginLogger.info("Player name changed: " + oldPlayerName + " -> " + currentPlayerName + " (" + player.getUniqueId() + ")");
            playerData.pastNames.add(oldPlayerName);
            this.playerRegistryConfig.playerRegistry.remove(oldPlayerName);
            this.playerRegistryConfig.playerRegistry.put(currentPlayerName, playerData);
            this.saveConfig();
        }

        // Do nothing for existing player with same name
    }

    public void logoutPlayer(@NotNull Player player) {
        var playerData = this.getPlayerDataById(player.getUniqueId());
        assert playerData != null;

        playerData.lastOnline = Instant.now().toString();
        this.saveConfig();
    }

    public String[] getAllPlayerNames() {
        return this.playerRegistryConfig.playerRegistry.keySet().toArray(String[]::new);
    }

    public PlayerRegistryConfiguration.PlayerData getPlayerDataByName(String playerName) {
        return this.playerRegistryConfig.playerRegistry.get(playerName);
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
