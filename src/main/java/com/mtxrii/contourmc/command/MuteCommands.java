package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SanctionService;
import com.mtxrii.contourmc.util.SearchUtil;
import com.mtxrii.contourmc.util.TimeUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandContainer
public final class MuteCommands {
    @Inject private RankService rankService;
    @Inject private SanctionService sanctionService;
    @Inject private PlayerRegistryService playerRegistryService;

    @Command("mute <player> <duration> <units> <reason>")
    public void mutePlayer(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName,
            @Argument(value = "duration") final long duration,
            @Argument(value = "units", suggestions = "timeUnits") final String units,
            @Argument(value = "reason") @Greedy final String reason
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.MOD, Rank.MEDIATOR, player);
        }

        Pair<UUID, String> parsedPlayerName = this.parsePlayerName(playerName);
        if (parsedPlayerName == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "No player found with name {}",
                    playerName
            ).sendTo(sender);
            return;
        }
        final UUID targetPlayerId = parsedPlayerName.getLeft();
        final String targetPlayerName = parsedPlayerName.getRight();

        if (duration <= 0) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "Duration must be greater than 0"
            ).sendTo(sender);
            return;
        }

        final TimeUtil.TimeUnit timeUnit = TimeUtil.TimeUnit.fromString(units);
        if (timeUnit == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "Time unit not recognized. Should be one of {}",
                    String.valueOf(TimeUtil.TimeUnit.getNormalizedNames()) // @TODO: Use Message.multi()
            ).sendTo(sender);
            return;
        }

        this.sanctionService.mute(targetPlayerId, reason, TimeUtil.getInstantInTimeFromNow(duration, timeUnit));
    }

    @Command("unmute <player>")
    public void unmutePlayer(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.MOD, Rank.MEDIATOR, player);
        }

        Pair<UUID, String> parsedPlayerName = this.parsePlayerName(playerName);
        if (parsedPlayerName == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "No player found with name {}",
                    playerName
            ).sendTo(sender);
            return;
        }
        final UUID targetPlayerId = parsedPlayerName.getLeft();
        final String targetPlayerName = parsedPlayerName.getRight();

        SanctionsConfiguration.Sanction previousMute = this.sanctionService.getMute(targetPlayerId);
        if (previousMute == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "Player {} is not muted",
                    targetPlayerName
            ).sendTo(sender);
            return;
        }

        this.sanctionService.unmute(targetPlayerId);
        new Message(
                MessagePrefix.MOD,
                "Unmuted {}, previously muted with reason: {}",
                targetPlayerName,
                previousMute.reason
        ).sendTo(sender);
    }

    // @TODO: Move this to const class
    @Suggestions("onlinePlayers")
    public @NotNull Set<String> suggestOnlinePlayers(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return Bukkit.getOnlinePlayers()
                     .stream()
                     .map(Player::getName)
                     .collect(Collectors.toSet());
    }

    @Suggestions("timeUnits")
    public @NotNull Set<String> suggestTimeUnits(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return TimeUtil.TimeUnit.getNormalizedNames();
    }

    // @TODO: Move this to util class
    private Pair<UUID, String> parsePlayerName(String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID targetPlayerId;
        String targetPlayerName;
        if (targetPlayer == null) {
            String offlinePlayerName = SearchUtil.findClosestMatch(
                    playerName,
                    this.playerRegistryService.getAllPlayerNames()
            );

            if (offlinePlayerName == null) {
                return null;
            }

            var offlinePlayerData = this.playerRegistryService.getPlayerDataByName(offlinePlayerName);
            targetPlayerId = offlinePlayerData.uniqueId;
            targetPlayerName = offlinePlayerData.name;

        } else {
            targetPlayerId = targetPlayer.getUniqueId();
            targetPlayerName = targetPlayer.getName();
        }

        return Pair.of(targetPlayerId, targetPlayerName);
    }
}
