package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessageConst;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.util.TimeUtil;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@Component
public class SanctionService {
    private final JacksonConfigurationLoader configLoader;
    private final SanctionsConfiguration sanctionsConfig;
    private final Logger pluginLogger;

    @Inject
    public SanctionService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                                                      .path(plugin.getDataPath().resolve("sanctions.json"))
                                                      .build();
        try {
            this.sanctionsConfig = this.configLoader.load().get(SanctionsConfiguration.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        this.pluginLogger = plugin.getLogger();
    }

    public void kick(Player target, String reason) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                &b&lYou've been kicked!
                
                {}
                
                
                """,
                reason
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICK_COMMAND);

        this.pluginLogger.info("Kicked " + target.getName() + " with reason: '" + reason + "'");
    }

    public void kick(Player target) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                &b&lYou've been kicked!
                
                
                """
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICK_COMMAND);

        this.pluginLogger.info("Kicked " + target.getName());
    }

    public void mute(UUID playerId, String reason, Instant expiration) {
        SanctionsConfiguration.Sanction previousMute = this.sanctionsConfig.mutes.get(playerId);

        SanctionsConfiguration.Sanction newMute = new SanctionsConfiguration.Sanction();
        newMute.reason = reason;
        newMute.expiresAt = TimeUtil.instantToString(expiration);
        this.sanctionsConfig.mutes.put(playerId, newMute);

        String muteLog = "Muted " + playerId + " with reason: '" + reason + "' until " + expiration;
        if (previousMute != null) {
            muteLog += " (replaces previously applied mute with reason: '" + previousMute.reason + "')";
        }
        this.pluginLogger.info(muteLog);
    }

    public void unmute(UUID playerId) {
        SanctionsConfiguration.Sanction currentMute = this.sanctionsConfig.mutes.get(playerId);
        if (currentMute != null) {
            this.sanctionsConfig.mutes.remove(playerId);

            this.pluginLogger.info(
                    "Unmuted " + playerId +
                    " (previously muted with reason: '" + currentMute.reason + "')"
            );
        }
    }

    public boolean isMuted(UUID playerId) {
        return this.sanctionsConfig.mutes.containsKey(playerId);
    }

    public SanctionsConfiguration.Sanction getMute(UUID playerId) {
        SanctionsConfiguration.Sanction currentMute = this.sanctionsConfig.mutes.get(playerId);
        if (currentMute == null) {
            return null;
        }

        SanctionsConfiguration.Sanction currentMuteCopy = new SanctionsConfiguration.Sanction();
        currentMuteCopy.reason = currentMute.reason;
        currentMuteCopy.expiresAt = currentMute.expiresAt;
        return currentMuteCopy;
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.sanctionsConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
