package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.config.PlayerRegistryConfiguration;
import com.mtxrii.contourmc.util.SearchUtil;
import com.mtxrii.contourmc.util.TimeUtil;
import com.sxtanna.platform.archetype.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@Component
@Singleton
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
            PlayerRegistryConfiguration loadedConfig = this.configLoader.load().get(PlayerRegistryConfiguration.class);
            this.playerRegistryConfig = loadedConfig != null ? loadedConfig : new PlayerRegistryConfiguration();
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
            newPlayerData.firstOnline = TimeUtil.instantToString(Instant.now());
            newPlayerData.lastOnline = TimeUtil.instantToString(Instant.now());
            newPlayerData.currentKit = "NONE";
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
        var playerData = this.getPlayerDataByName(player.getName());
        assert playerData != null;

        playerData.lastOnline = TimeUtil.instantToString(Instant.now());

        this.playerRegistryConfig.playerRegistry.put(player.getName(), playerData);
        this.saveConfig();
    }

    public String[] getAllPlayerNames() {
        return this.playerRegistryConfig.playerRegistry.keySet().toArray(String[]::new);
    }

    public PlayerRegistryConfiguration.PlayerData getPlayerDataByName(String playerName) {
        return this.playerRegistryConfig.playerRegistry.get(playerName);
    }

    public String getPlayerNameById(UUID playerId) {
        PlayerRegistryConfiguration.PlayerData playerData = this.getPlayerDataById(playerId);
        assert playerData != null;
        return playerData.name;
    }

    public void setCurrentKit(UUID playerId, String kitName) {
        var playerData = this.getPlayerDataById(playerId);
        assert playerData != null;
        playerData.currentKit = kitName;
        this.saveConfig();
    }

    public String getCurrentKit(UUID playerId) {
        var playerData = this.getPlayerDataById(playerId);
        assert playerData != null;
        return playerData.currentKit;
    }

    /// Gets a partial player name and looks it up in online players first then using {@link SearchUtil#findClosestMatch}.
    ///
    /// If found, returns a {@code Pair<UUID, String>} with the found player's uuid and full name capitalized correctly.
    ///
    /// Otherwise, returns null.
    public Pair<UUID, String> parsePlayerName(String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID targetPlayerId;
        String targetPlayerName;
        if (targetPlayer == null) {
            String offlinePlayerName = SearchUtil.findClosestMatch(
                    playerName,
                    this.getAllPlayerNames()
            );

            if (offlinePlayerName == null) {
                return null;
            }

            var offlinePlayerData = this.getPlayerDataByName(offlinePlayerName);
            targetPlayerId = offlinePlayerData.uniqueId;
            targetPlayerName = offlinePlayerData.name;

        } else {
            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerName = targetPlayer.getName();
        }

        return Pair.of(targetPlayerId, targetPlayerName);
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
