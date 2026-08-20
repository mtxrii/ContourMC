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

import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.logging.Logger;

@Component
@Singleton
public class PlayerRegistryService {
    private final JacksonConfigurationLoader configLoader;
    private final PlayerRegistryConfiguration playerRegistryConfig;
    private final Logger pluginLogger;
    private final IpTimezoneLookupService ipTimezoneLookupService;

    @Inject
    public PlayerRegistryService(Plugin plugin, IpTimezoneLookupService ipTimezoneLookupService) {
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
        this.ipTimezoneLookupService = ipTimezoneLookupService;
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
            this.updateTimezone(player, newPlayerData);
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

        // Existing player
        this.updateRegularlyOnlinePlayerRoutine(player, playerData);
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

    public String getTimezone(UUID playerId) {
        var playerData = this.getPlayerDataById(playerId);
        assert playerData != null;
        return playerData.timezone;
    }

    public String formatInstantForPlayer(Instant instant, UUID playerId) {
        String timezone = this.getTimezone(playerId);
        return TimeUtil.formatInstantForPlayer(instant, timezone);
    }

    public String formatInstantForPlayer(String instantString, UUID playerId) {
        String timezone = this.getTimezone(playerId);
        return TimeUtil.formatInstantForPlayer(TimeUtil.stringToInstant(instantString), timezone);
    }

    /// Gets a partial player name and looks it up in online players first then using {@link SearchUtil#findClosestPlayerNameMatch}.
    ///
    /// If found, returns a {@code Pair<UUID, String>} with the found player's uuid and full name capitalized correctly.
    ///
    /// Otherwise, returns null.
    public Pair<UUID, String> parsePlayerName(String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID targetPlayerId;
        String targetPlayerName;
        if (targetPlayer == null) {
            String offlinePlayerName = SearchUtil.findClosestPlayerNameMatch(
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

    private void updateRegularlyOnlinePlayerRoutine(Player player, PlayerRegistryConfiguration.PlayerData playerData) {
        // Update IP if changed since their last session
        if (this.updateIp(player, playerData)) {
            this.saveConfig();
        }
        // Refresh the timezone as IP geolocation can change between sessions
        if (this.updateTimezone(player, playerData)) {
            this.saveConfig();
        }
    }

    public String getPlayerIp(UUID playerId) {
        Player onlinePlayer = Bukkit.getPlayer(playerId);
        if (onlinePlayer != null) {
            InetSocketAddress currentInetSocketAddress = onlinePlayer.getAddress();
            if (currentInetSocketAddress == null) {
                return "UNKNOWN";
            }
            return currentInetSocketAddress.getAddress().getHostAddress();
        } else {
            var playerData = this.getPlayerDataById(playerId);
            assert playerData != null;
            return playerData.lastIp;
        }
    }

    private PlayerRegistryConfiguration.PlayerData getPlayerDataById(UUID playerId) {
        for (var playerData : this.playerRegistryConfig.playerRegistry.values()) {
            if (playerData.uniqueId.equals(playerId)) {
                return playerData;
            }
        }
        return null;
    }

    private boolean updateTimezone(Player player, PlayerRegistryConfiguration.PlayerData playerData) {
        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            return false;
        }

        ZoneId timezone = this.ipTimezoneLookupService.lookup(player.getAddress().getAddress()).orElse(null);
        if (timezone == null || timezone.getId().equals(playerData.timezone)) {
            return false;
        }

        playerData.timezone = timezone.getId();
        return true;
    }

    private boolean updateIp(Player player, PlayerRegistryConfiguration.PlayerData playerData) {
        String lastIp = playerData.lastIp;
        if (lastIp == null) {
            lastIp = "UNKNOWN";
        }
        String currentIp = this.getPlayerIp(player.getUniqueId());
        if (!lastIp.equals(currentIp)) {
            this.pluginLogger.info(
                    "Player " + player.getName() + "'s IP changed: " + lastIp + " -> " + currentIp +
                    " (" + player.getUniqueId() + ")"
            );
            playerData.lastIp = currentIp;
            return true;
        }
        return false;
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.playerRegistryConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
