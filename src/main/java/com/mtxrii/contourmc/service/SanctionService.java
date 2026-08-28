package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessageConst;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.util.TimeUtil;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@Component
@Singleton
public class SanctionService {
    private final JacksonConfigurationLoader configLoader;
    private final SanctionsConfiguration sanctionsConfig;
    private final PlayerRegistryService playerRegistryService;
    private final Logger pluginLogger;

    @Inject
    public SanctionService(Plugin plugin, PlayerRegistryService playerRegistryService) {
        this.configLoader = JacksonConfigurationLoader.builder()
                                                      .path(plugin.getDataPath().resolve("sanctions.json"))
                                                      .build();
        try {
            SanctionsConfiguration loadedConfig = this.configLoader.load().get(SanctionsConfiguration.class);
            this.sanctionsConfig = loadedConfig != null ? loadedConfig : new SanctionsConfiguration();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        this.playerRegistryService = playerRegistryService;
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

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICKED);

        this.pluginLogger.info("Kicked " + target.getName() + " with reason: '" + reason + "'");
    }

    public void kick(Player target) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                &b&lYou've been kicked!
                
                
                """
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICKED);

        this.pluginLogger.info("Kicked " + target.getName());
    }

    public void mute(UUID playerId, String reason, Instant expiration) {
        SanctionsConfiguration.Sanction previousMute = this.getMute(playerId);

        SanctionsConfiguration.Sanction newMute = new SanctionsConfiguration.Sanction();
        newMute.reason = reason;
        newMute.expiresAt = TimeUtil.instantToString(expiration);
        this.sanctionsConfig.mutes.put(playerId, newMute);
        this.saveConfig();

        String muteLog = "Muted " + playerId + " with reason: '" + reason + "' until " + expiration;
        if (previousMute != null) {
            muteLog += " (replaces previously applied mute with reason: '" + previousMute.reason + "')";
        }
        this.pluginLogger.info(muteLog);
    }

    public void unmute(UUID playerId) {
        SanctionsConfiguration.Sanction currentMute = this.getMute(playerId);
        if (currentMute != null) {
            this.sanctionsConfig.mutes.remove(playerId);
            this.saveConfig();

            this.pluginLogger.info(
                    "Unmuted " + playerId +
                    " (previously muted with reason: '" + currentMute.reason + "')"
            );
        }
    }

    public boolean isMuted(UUID playerId) {
        this.checkMuteTimeLeft(playerId);
        return this.sanctionsConfig.mutes.containsKey(playerId);
    }

    public SanctionsConfiguration.Sanction getMute(UUID playerId) {
        this.checkMuteTimeLeft(playerId);
        SanctionsConfiguration.Sanction currentMute = this.sanctionsConfig.mutes.get(playerId);
        if (currentMute == null) {
            return null;
        }

        SanctionsConfiguration.Sanction currentMuteCopy = new SanctionsConfiguration.Sanction();
        currentMuteCopy.reason = currentMute.reason;
        currentMuteCopy.expiresAt = currentMute.expiresAt;
        return currentMuteCopy;
    }

    public void ban(UUID playerId, String reason, Instant expiration) {
        SanctionsConfiguration.Sanction previousBan = this.getBan(playerId);
        String expirationString = TimeUtil.instantToString(expiration);

        SanctionsConfiguration.Sanction newBan = new SanctionsConfiguration.Sanction();
        newBan.reason = reason;
        newBan.expiresAt = expirationString;
        this.sanctionsConfig.bans.put(playerId, newBan);
        this.saveConfig();

        String targetPlayerName = this.playerRegistryService.getPlayerNameById(playerId);
        if (targetPlayerName != null) {
            Player target = Bukkit.getPlayer(targetPlayerName);
            if (target != null && target.isOnline()) {
                Message kickMessage = this.getBanMessage(playerId, reason, expirationString);
                target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.BANNED);
            }
        }

        String banLog = "Banned " + playerId + " with reason: '" + reason + "' until " + expiration;
        if (previousBan != null) {
            banLog += " (replaces previously applied ban with reason: '" + previousBan.reason + "')";
        }
        this.pluginLogger.info(banLog);
    }

    public void unban(UUID playerId) {
        SanctionsConfiguration.Sanction currentBan = this.getBan(playerId);
        if (currentBan != null) {
            this.sanctionsConfig.bans.remove(playerId);
            this.saveConfig();

            this.pluginLogger.info(
                    "Unbanned " + playerId +
                    " (previously banned with reason: '" + currentBan.reason + "')"
            );
        }
    }

    public boolean isBanned(UUID playerId) {
        this.checkBanTimeLeft(playerId);
        return this.sanctionsConfig.bans.containsKey(playerId);
    }

    public SanctionsConfiguration.Sanction getBan(UUID playerId) {
        this.checkBanTimeLeft(playerId);
        SanctionsConfiguration.Sanction currentBan = this.sanctionsConfig.bans.get(playerId);
        if (currentBan == null) {
            return null;
        }

        SanctionsConfiguration.Sanction currentBanCopy = new SanctionsConfiguration.Sanction();
        currentBanCopy.reason = currentBan.reason;
        currentBanCopy.expiresAt = currentBan.expiresAt;
        return currentBanCopy;
    }

    public Message getBanMessage(UUID playerId, String reason, String expirationString) {
        if (!this.isBanned(playerId)) {
            return null;
        }

        return MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                &b&lYou've been banned!
                
                &bReason: {}
                
                &bUntil: {}
                
                
                """,
                reason,
                this.playerRegistryService.formatInstantForPlayer(expirationString, playerId)
        )).append(MessageConst.SANCTION_MSG_BORDER);
    }

    private void checkMuteTimeLeft(UUID playerId) {
        SanctionsConfiguration.Sanction currentMute = this.sanctionsConfig.mutes.get(playerId);
        if (currentMute == null) {
            return;
        }

        Instant expiration = TimeUtil.stringToInstant(currentMute.expiresAt);
        if (Instant.now().isAfter(expiration)) {
            this.sanctionsConfig.mutes.remove(playerId);
            this.saveConfig();
        }
    }

    private void checkBanTimeLeft(UUID playerId) {
        SanctionsConfiguration.Sanction currentBan = this.sanctionsConfig.bans.get(playerId);
        if (currentBan == null) {
            return;
        }

        Instant expiration = TimeUtil.stringToInstant(currentBan.expiresAt);
        if (Instant.now().isAfter(expiration)) {
            this.sanctionsConfig.bans.remove(playerId);
            this.saveConfig();
        }
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.sanctionsConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
