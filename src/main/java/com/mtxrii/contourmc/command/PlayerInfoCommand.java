package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.config.PlayerRegistryConfiguration;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SanctionService;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.mtxrii.contourmc.util.GameUtil;
import com.mtxrii.contourmc.util.SearchUtil;
import com.mtxrii.contourmc.util.TextUtil;
import com.mtxrii.contourmc.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@CommandContainer
public final class PlayerInfoCommand {
    @Inject private RankService rankService;
    @Inject private PlayerRegistryService playerRegistryService;
    @Inject private SpawnpointService spawnpointService;
    @Inject private SanctionService sanctionService;

    @Command("playerinfo <player>")
    @CommandDescription("Gets some information about a player")
    public void playerInfo(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        Message response;
        boolean canSenderSeePrivateInfo = this.canSenderSeePrivateInfo(sender);
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            String offlinePlayerName = SearchUtil.findClosestPlayerNameMatch(
                    playerName,
                    this.playerRegistryService.getAllPlayerNames()
            );

            if (offlinePlayerName == null) {
                new Message(
                        MessagePrefix.INFO,
                        true,
                        "Player not found"
                ).sendTo(sender);
                return;
            }

            response = this.compileMsgOfflinePlayer(offlinePlayerName, canSenderSeePrivateInfo);
        } else {
            response = this.compileMsgOnlinePlayer(player, canSenderSeePrivateInfo);
        }
        response.sendTo(sender);
    }

    @Suggestions("onlinePlayers")
    public @NotNull Set<String> suggestOnlinePlayers(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return GameUtil.getOnlinePlayers();
    }

    private boolean canSenderSeePrivateInfo(Source sender) {
        if (sender.source() instanceof Player player) {
            return this.rankService.getRank(player.getUniqueId()).isAtLeast(Rank.MEDIATOR);
        }
        return true;
    }

    private Message compileMsgOnlinePlayer(Player player, boolean showPrivateInfo) {
        double playerCurrentHealth = Math.round(player.getHealth() * 10.0) / 10.0;
        double playerMaxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        String healthStr = '(' + String.valueOf(playerCurrentHealth) + '/' + playerMaxHealth + ')';
        var playerData = this.playerRegistryService.getPlayerDataByName(player.getName());

        String messageTemplate = "{}:" +
                "\nOnline: {}" +
                "\nRank: {}" +
                "\nLocation: {}" +
                "\nLast spawn: {}" +
                "\nHealth: {}" +
                "\nJoin Date: {}" +
                "\nCurrent kit: {}" +
                "\nTimezone: {}" +
                "\nPast Names: {}";
        Message response = new Message(
                MessagePrefix.INFO,
                messageTemplate,
                player.getName(),
                String.valueOf(true),
                TextUtil.formatEnumName(this.rankService.getRank(player.getUniqueId())),
                TextUtil.formatLocation(player.getLocation()),
                this.spawnpointService.getLastSpawnpointForPlayer(player.getUniqueId()),
                healthStr,
                TextUtil.formatInstant(TimeUtil.stringToInstant(playerData.firstOnline)),
                this.playerRegistryService.getCurrentKit(player.getUniqueId()),
                this.playerRegistryService.getTimezone(player.getUniqueId()),
                formatPastNames(playerData)
        );

        if (showPrivateInfo) {
            boolean isMuted = this.sanctionService.isMuted(player.getUniqueId());
            if (isMuted) {
                SanctionsConfiguration.Sanction mute = this.sanctionService.getMute(player.getUniqueId());
                response = response.append(new Message(
                        MessagePrefix.BLANK,
                        "\nMuted: {}" +
                        "\nMutedReason: {}" +
                        "\nMutedUntil: {}",
                        String.valueOf(true),
                        mute.reason,
                        TimeUtil.formatInstantForPlayer(mute.expiresAt)
                ));
            } else {
                response = response.append(new Message(
                        MessagePrefix.BLANK,
                        "\nMuted: {}",
                        String.valueOf(false)
                ));
            }
        }

        return response;
    }

    private Message compileMsgOfflinePlayer(String playerName, boolean showPrivateInfo) {
        var offlinePlayerData = this.playerRegistryService.getPlayerDataByName(playerName);
        UUID offlinePlayerId = offlinePlayerData.uniqueId;
        String messageTemplate = "{}:" +
                "\nOnline: {}" +
                "\nRank: {}" +
                "\nLast Online: {}" +
                "\nLast kit: {}" +
                "\nTimezone: {}" +
                "\nPast Names: {}";
        Message response = new Message(
                MessagePrefix.INFO,
                messageTemplate,
                offlinePlayerData.name,
                String.valueOf(false),
                TextUtil.formatEnumName(this.rankService.getRank(offlinePlayerId)),
                TextUtil.formatInstant(TimeUtil.stringToInstant(offlinePlayerData.lastOnline)),
                offlinePlayerData.currentKit,
                this.playerRegistryService.getTimezone(offlinePlayerId),
                formatPastNames(offlinePlayerData)
        );

        if (showPrivateInfo) {
            boolean isMuted = this.sanctionService.isMuted(offlinePlayerId);
            if (isMuted) {
                SanctionsConfiguration.Sanction mute = this.sanctionService.getMute(offlinePlayerId);
                response = response.append(new Message(
                        MessagePrefix.BLANK,
                        "\nMuted: {}" +
                        "\nMutedReason: {}" +
                        "\nMutedUntil: {}",
                        String.valueOf(true),
                        mute.reason,
                        TimeUtil.formatInstantForPlayer(mute.expiresAt)
                ));
            } else {
                response = response.append(new Message(
                        MessagePrefix.BLANK,
                        "\nMuted: {}",
                        String.valueOf(false)
                ));
            }

            boolean isBanned = this.sanctionService.isBanned(offlinePlayerId);
            if (isBanned) {
                SanctionsConfiguration.Sanction ban = this.sanctionService.getBan(offlinePlayerId);
                response = response.append(new Message(
                        MessagePrefix.BLANK,
                        "\nBanned: {}" +
                        "\nBannedReason: {}" +
                        "\nBannedUntil: {}",
                        String.valueOf(true),
                        ban.reason,
                        TimeUtil.formatInstantForPlayer(ban.expiresAt)
                ));
            } else {
                response = response.append(new Message(
                        MessagePrefix.BLANK,
                        "\nBanned: {}",
                        String.valueOf(false)
                ));
            }
        }
        return response;
    }

    private static String formatPastNames(PlayerRegistryConfiguration.PlayerData playerData) {
        if (playerData == null) {
            return "None";
        }
        return String.join(", ", playerData.pastNames);
    }
}
