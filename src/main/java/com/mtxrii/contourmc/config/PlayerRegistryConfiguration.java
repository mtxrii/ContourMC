package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class PlayerRegistryConfiguration {
    @Setting
    public Map<String, PlayerData> playerRegistry = new LinkedHashMap<>();

    @ConfigSerializable
    public static class PlayerData {
        @Setting
        public UUID uniqueId;

        @Setting
        public String name;

        @Setting
        public Instant lastOnline;
    }
}
