package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class SanctionsConfiguration {
    @Setting public Map<UUID, Sanction> mutes = new LinkedHashMap<>();

    @Setting public Map<UUID, Sanction> bans = new LinkedHashMap<>();

    @ConfigSerializable
    public static class Sanction {
        @Setting public String reason;

        /// Stored as a {@code java.time.Instant} string representation
        @Setting public String expiresAt;
    }
}
