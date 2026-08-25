package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigSerializable
public class ZiplineConfiguration {
    @Setting
    public Map<String, Zipline> ziplines = new LinkedHashMap<>();

    @ConfigSerializable
    public static class Zipline {
        @Setting
        public String startWorld;

        @Setting
        public double startX;

        @Setting
        public double startY;

        @Setting
        public double startZ;

        @Setting
        public String endWorld;

        @Setting
        public double endX;

        @Setting
        public double endY;

        @Setting
        public double endZ;
    }
}
