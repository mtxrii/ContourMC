package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.config.PlayerStatsConfiguration;
import com.sxtanna.platform.archetype.Component;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Persists player PvP statistics independently of player names.
 *
 * <p>UUID keys keep statistics attached to a player when their name changes.
 * A kill increments the killer's total kills and killstreak, while a death
 * increments the victim's deaths and resets their killstreak.</p>
 */
@Component
@Singleton
public class PlayerStatsService {
    private final JacksonConfigurationLoader configLoader;
    private final PlayerStatsConfiguration statsConfig;

    @Inject
    public PlayerStatsService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                .path(plugin.getDataPath().resolve("playerStats.json"))
                .build();
        try {
            PlayerStatsConfiguration loadedConfig = this.configLoader.load().get(PlayerStatsConfiguration.class);
            this.statsConfig = loadedConfig != null ? loadedConfig : new PlayerStatsConfiguration();
            if (this.statsConfig.playerStats == null) {
                this.statsConfig.playerStats = new java.util.LinkedHashMap<>();
            }
        } catch (ConfigurateException exception) {
            throw new RuntimeException(exception);
        }
    }

    @NotNull
    public PlayerStatsConfiguration.PlayerStats getStats(@NotNull UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return this.statsConfig.playerStats.computeIfAbsent(
                playerId,
                ignored -> new PlayerStatsConfiguration.PlayerStats()
        );
    }

    public int getKills(@NotNull UUID playerId) {
        return this.getStats(playerId).kills;
    }

    public int getDeaths(@NotNull UUID playerId) {
        return this.getStats(playerId).deaths;
    }

    public int getKillstreak(@NotNull UUID playerId) {
        return this.getStats(playerId).killstreak;
    }

    /** Records a death without a player killer, such as a fall or explosion. */
    public void recordDeath(@NotNull UUID victimId) {
        PlayerStatsConfiguration.PlayerStats victimStats = this.getStats(victimId);
        victimStats.deaths++;
        victimStats.killstreak = 0;
        this.saveConfig();
    }

    /** Records a player-versus-player kill and persists both players together. */
    public void recordKillAndDeath(@NotNull UUID killerId, @NotNull UUID victimId) {
        Objects.requireNonNull(killerId, "killerId");
        Objects.requireNonNull(victimId, "victimId");

        if (killerId.equals(victimId)) {
            this.recordDeath(victimId);
            return;
        }

        PlayerStatsConfiguration.PlayerStats killerStats = this.getStats(killerId);
        killerStats.kills++;
        killerStats.killstreak++;

        PlayerStatsConfiguration.PlayerStats victimStats = this.getStats(victimId);
        victimStats.deaths++;
        victimStats.killstreak = 0;

        this.saveConfig();
    }

    /** Returns all known statistics for future leaderboard and command use. */
    @NotNull
    public Map<UUID, PlayerStatsConfiguration.PlayerStats> getAllStats() {
        return Collections.unmodifiableMap(this.statsConfig.playerStats);
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.statsConfig));
        } catch (ConfigurateException exception) {
            throw new RuntimeException(exception);
        }
    }
}
