package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.config.RanksConfiguration;
import com.mtxrii.contourmc.exception.InsufficientPermissionException;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.util.UUID;
import java.util.logging.Logger;

@Component
@Singleton
public class RankService {
    private final JacksonConfigurationLoader configLoader;
    private final RanksConfiguration ranksConfig;
    private final Logger pluginLogger;

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
        this.pluginLogger = plugin.getLogger();
    }

    public void setRank(@NotNull UUID playerId, @NotNull Rank newRank) {
        this.ranksConfig.ranks.put(playerId, newRank);
        this.saveConfig();
    }

    public Rank getRank(@NotNull UUID playerId) {
        return this.ranksConfig.ranks.get(playerId);
    }

    public void requireRank(@NotNull MessagePrefix prefix, @NotNull Rank requiredRank, @NotNull Player player) {
        if (this.getRank(player.getUniqueId()).isAtLeast(requiredRank)) {
            return;
        }
        this.pluginLogger.warning(
                "Player " + player.getName() + " tried to run an action with insufficient permissions. " +
                "Required rank: " + requiredRank + ", player rank: " + this.getRank(player.getUniqueId())
        );
        throw new InsufficientPermissionException(prefix);
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.ranksConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
