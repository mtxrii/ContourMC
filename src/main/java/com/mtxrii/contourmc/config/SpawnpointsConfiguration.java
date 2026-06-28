package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigSerializable
public class SpawnpointsConfiguration {
    @Setting
    public Map<String, Spawnpoint> spawnpoints = new LinkedHashMap<>();

    @ConfigSerializable
    public static class Spawnpoint {
        @Setting
        public String world;

        @Setting
        public double x;

        @Setting
        public double y;

        @Setting
        public double z;

        @Setting
        public float yaw;

        @Setting
        public float pitch;
    }
}
