package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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

        /// Stored as a {@code java.time.Instant} string representation
        @Setting
        public String firstOnline;

        /// Stored as a {@code java.time.Instant} string representation
        @Setting
        public String lastOnline;

        @Setting
        public Set<String> pastNames;

        @Setting
        public String currentKit;

        /// IANA timezone identifier resolved from the player's most recently seen IP address.
        /// This is null when the local GeoIP database has no timezone for the address.
        @Setting
        public String timezone;
    }
}
