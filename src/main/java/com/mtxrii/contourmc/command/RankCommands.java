package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.util.TextUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandContainer
public final class RankCommands {
    private static Message getRanksMessage = null;

    @Inject private RankService rankService;
    @Inject private PlayerRegistryService playerRegistryService;

    @Command("setrank <player> <rank>")
    public void setRank(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName,
            @Argument(value = "rank", suggestions = "ranks") final String rankName
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.ENV, Rank.STAFF, player);
        }

        Rank newRank = Rank.get(rankName);
        if (newRank == null) {
            new Message(
                    MessagePrefix.RANK,
                    true,
                    "No rank with name {} exists",
                    rankName.toLowerCase()
            ).sendTo(sender);
            return;
        }

        Pair<UUID, String> parsedPlayerName = this.playerRegistryService.parsePlayerName(playerName);
        if (parsedPlayerName == null) {
            TextUtil.getNoPlayerFoundMessage(MessagePrefix.RANK, playerName).sendTo(sender);
            return;
        }

        UUID targetPlayerId = parsedPlayerName.getLeft();
        String targetPlayerName = parsedPlayerName.getRight();

        this.rankService.setRank(targetPlayerId, newRank);
        new Message(
                MessagePrefix.RANK,
                "Set {} rank to {}",
                targetPlayerName + "'s",
                TextUtil.formatEnumName(newRank)
        ).sendTo(sender);

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null) {
            new Message(
                    MessagePrefix.RANK,
                    "Your rank has been set to {}",
                    TextUtil.formatEnumName(newRank)
            ).sendTo(targetPlayer);
        }
    }

    @Command("getrank <player>")
    public void getRank(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        Pair<UUID, String> parsedPlayerName = this.playerRegistryService.parsePlayerName(playerName);
        if (parsedPlayerName == null) {
            TextUtil.getNoPlayerFoundMessage(MessagePrefix.RANK, playerName).sendTo(sender);
            return;
        }

        UUID targetPlayerId = parsedPlayerName.getLeft();
        String targetPlayerName = parsedPlayerName.getRight();

        Rank rank = this.rankService.getRank(targetPlayerId);
        new Message(
                MessagePrefix.RANK,
                "{} is rank {}",
                targetPlayerName,
                TextUtil.formatEnumName(rank)
        ).sendTo(sender);
    }

    @Command("ranks")
    public void ranks(
            @NotNull final Source sender
    ) {
        if (getRanksMessage != null) {
            getRanksMessage.sendTo(sender);
            return;
        }

        Rank[] ranks = Rank.values();
        String[] rankNames = new String[ranks.length];
        for (int i = 0; i < ranks.length; i++) {
            Rank rank = ranks[i];
            rankNames[i] = TextUtil.formatEnumName(rank);
        }

        Message ranksListMsg = new Message(
                MessagePrefix.RANK,
                "Available ranks: {}",
                Message.multi(rankNames)
        );
        ranksListMsg.sendTo(sender);
        getRanksMessage = ranksListMsg;
    }

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

    @Suggestions("ranks")
    public @NotNull Set<String> suggestRanks(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return Arrays.stream(Rank.values())
                     .map(rank -> rank.name().toLowerCase())
                     .collect(Collectors.toSet());
    }
}
