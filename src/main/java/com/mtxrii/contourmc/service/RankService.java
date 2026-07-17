package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.config.RanksConfiguration;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.util.UUID;

@Component
@Singleton
public class RankService {
    private final JacksonConfigurationLoader configLoader;
    private final RanksConfiguration ranksConfig;

    @Inject
    public RankService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                .path(plugin.getDataPath().resolve("ranks.json"))
                .build();
        try {
            this.ranksConfig = this.configLoader.load().get(RanksConfiguration.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRank(@NotNull UUID playerId, @NotNull Rank newRank) {
        this.ranksConfig.ranks.put(playerId, newRank);
        this.saveConfig();
    }

    public Rank getRank(@NotNull UUID playerId) {
        return this.ranksConfig.ranks.get(playerId);
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.ranksConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
