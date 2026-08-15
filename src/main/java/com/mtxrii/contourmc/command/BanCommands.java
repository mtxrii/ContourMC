package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.config.SanctionsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SanctionService;
import com.mtxrii.contourmc.util.TextUtil;
import com.mtxrii.contourmc.util.TimeUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@CommandContainer
public final class BanCommands {
    @Inject private RankService rankService;
    @Inject private SanctionService sanctionService;
    @Inject private PlayerRegistryService playerRegistryService;

    @Command("ban <player> <duration> <units> <reason>")
    @CommandDescription("Bans a player for a specified duration with a given reason")
    public void banPlayer(
            @NotNull final Source sender,
            @Argument(value = "player") final String playerName,
            @Argument(value = "duration") final long duration,
            @Argument(value = "units", suggestions = "timeUnits") final String units,
            @Argument(value = "reason") @Greedy final String reason
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.MOD, Rank.MEDIATOR, player);
        }

        Pair<UUID, String> parsedPlayerName = this.playerRegistryService.parsePlayerName(playerName);
        if (parsedPlayerName == null) {
            TextUtil.getNoPlayerFoundMessage(MessagePrefix.MOD, playerName).sendTo(sender);
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
                    Message.multi(TimeUtil.TimeUnit.getNormalizedNames())
            ).sendTo(sender);
            return;
        }

        if (this.sanctionService.isBanned(targetPlayerId)) {
            SanctionsConfiguration.Sanction previousBan = this.sanctionService.getBan(targetPlayerId);
            new Message(
                    MessagePrefix.MOD,
                    "Player {} was already banned for reason: {}. Ban overridden by yours",
                    targetPlayerName,
                    previousBan.reason
            ).sendTo(sender);
        }

        Instant unbanAt = TimeUtil.getInstantInTimeFromNow(duration, timeUnit);
        this.sanctionService.ban(targetPlayerId, reason, unbanAt);
        new Message(
                MessagePrefix.MOD,
                "Banned {} with reason {} until {}",
                playerName,
                reason,
                TimeUtil.formatInstantForPlayer(unbanAt)
        ).sendTo(sender);
    }

    @Command("unban <player>")
    @CommandDescription("Unbans a player")
    public void unbanPlayer(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.MOD, Rank.MEDIATOR, player);
        }

        Pair<UUID, String> parsedPlayerName = this.playerRegistryService.parsePlayerName(playerName);
        if (parsedPlayerName == null) {
            TextUtil.getNoPlayerFoundMessage(MessagePrefix.MOD, playerName).sendTo(sender);
            return;
        }
        final UUID targetPlayerId = parsedPlayerName.getLeft();
        final String targetPlayerName = parsedPlayerName.getRight();

        SanctionsConfiguration.Sanction previousBan = this.sanctionService.getBan(targetPlayerId);
        if (previousBan == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "Player {} is not banned",
                    targetPlayerName
            ).sendTo(sender);
            return;
        }

        this.sanctionService.unban(targetPlayerId);
        new Message(
                MessagePrefix.MOD,
                "Unbanned {}, previously banned with reason: {}",
                targetPlayerName,
                previousBan.reason
        ).sendTo(sender);
    }

    @Suggestions("timeUnits")
    public @NotNull Set<String> suggestTimeUnits(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return TimeUtil.TimeUnit.getNormalizedNames();
    }
}
