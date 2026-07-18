package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.util.TextUtil;
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
import java.util.stream.Collectors;

@CommandContainer
public final class RankCommand {
    private static Message getRanksMessage = null;

    @Inject private RankService rankService;

    @Command("setrank <player> <rank>")
    public void setRank(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName,
            @Argument(value = "rank", suggestions = "ranks") final String rankName
    ) {
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

        // @TODO: Add support for offline players
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            new Message(
                    MessagePrefix.RANK,
                    true,
                    "No player found with name {}",
                    playerName
            ).sendTo(sender);
            return;
        }

        this.rankService.setRank(targetPlayer.getUniqueId(), newRank);
        new Message(
                MessagePrefix.RANK,
                "Set {} rank to {}",
                targetPlayer.getName() + "'s",
                TextUtil.formatEnumName(newRank)
        ).sendTo(sender);
        new Message(
                MessagePrefix.RANK,
                "Your rank has been set to {}",
                TextUtil.formatEnumName(newRank)
        ).sendTo(targetPlayer);
    }

    @Command("getrank <player>")
    public void getRank(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        // @TODO: Add support for offline players
        // @TODO: Make shared method for this block
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            new Message(
                    MessagePrefix.RANK,
                    true,
                    "No player found with name {}",
                    playerName
            ).sendTo(sender);
            return;
        }

        Rank rank = this.rankService.getRank(targetPlayer.getUniqueId());
        new Message(
                MessagePrefix.RANK,
                "{} is rank {}",
                targetPlayer.getName(),
                TextUtil.formatEnumName(rank)
        ).sendTo(sender);
    }

    @Command("ranks")
    public void getRanks(
            @NotNull final Source sender
    ) {
        if (getRanksMessage != null) {
            getRanksMessage.sendTo(sender);
            return;
        }

        String[] rankNames = new String[Rank.values().length];
        StringBuilder messageTemplate = new StringBuilder("Available ranks: ");
        Rank[] ranks = Rank.values();
        for (int i = 0; i < ranks.length; i++) {
            Rank rank = ranks[i];
            rankNames[i] = TextUtil.formatEnumName(rank);

            messageTemplate.append("{}");
            if (i != ranks.length - 1) {
                messageTemplate.append(", ");
            }
        }

        Message ranksListMsg = new Message(
                MessagePrefix.RANK,
                messageTemplate.toString(),
                rankNames
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
