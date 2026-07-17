package com.mtxrii.contourmc.config;

import com.mtxrii.contourmc.Rank;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class RanksConfiguration {
    @Setting
    public Map<UUID, Rank> ranks;
}
