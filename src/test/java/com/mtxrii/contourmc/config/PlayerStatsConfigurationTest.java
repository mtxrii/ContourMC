package com.mtxrii.contourmc.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerStatsConfigurationTest {

    @Test
    void createsAnEmptyStatsDocument() {
        PlayerStatsConfiguration configuration = new PlayerStatsConfiguration();

        assertThat(configuration.playerStats).isEmpty();
    }

    @Test
    void storesStatsByUuid() {
        PlayerStatsConfiguration configuration = new PlayerStatsConfiguration();
        UUID playerId = UUID.randomUUID();
        PlayerStatsConfiguration.PlayerStats stats = new PlayerStatsConfiguration.PlayerStats();
        stats.kills = 12;
        stats.deaths = 4;
        stats.killstreak = 3;

        configuration.playerStats.put(playerId, stats);

        assertThat(configuration.playerStats).containsEntry(playerId, stats);
        assertThat(configuration.playerStats.get(playerId).kills).isEqualTo(12);
        assertThat(configuration.playerStats.get(playerId).deaths).isEqualTo(4);
        assertThat(configuration.playerStats.get(playerId).killstreak).isEqualTo(3);
    }
}
