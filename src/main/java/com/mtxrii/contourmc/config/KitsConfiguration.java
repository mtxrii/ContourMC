package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@ConfigSerializable
public class KitsConfiguration {
    @Setting
    public Map<String, String> kits;
}
