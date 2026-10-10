package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class PlayerStatsConfiguration {
    @Setting
    public Map<UUID, PlayerStats> playerStats = new LinkedHashMap<>();

    @ConfigSerializable
    public static class PlayerStats {
        @Setting
        public int kills;

        @Setting
        public int deaths;

        @Setting
        public int killstreak;
    }
}
