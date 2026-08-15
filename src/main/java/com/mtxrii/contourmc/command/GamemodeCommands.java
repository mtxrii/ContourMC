package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.util.GameUtil;
import com.mtxrii.contourmc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@CommandContainer
public final class GamemodeCommands {
    @Inject private RankService rankService;

    @Command("gamemode|gm <gamemode> [player]")
    @CommandDescription("Changes your or another player's gamemode")
    public void gamemode(
            @NotNull final Source sender,
            @Argument(value = "gamemode", suggestions = "gamemodes") final String gamemode,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        if (!(sender instanceof PlayerSource) && playerName == null) {
            new Message(
                    MessagePrefix.GAME,
                    true,
                    "Since you are not a player, you must specify a player name to target"
            ).sendTo(sender);
            return;
        }

        Player targetPlayer = null;
        if (sender.source() instanceof Player playerSender) {
            this.rankService.requireRank(MessagePrefix.GAME, Rank.MEDIATOR, playerSender);
            if (playerName == null) {
                targetPlayer = playerSender;
            }
        }

        if (!TextUtil.isEmpty(playerName)) {
            targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                new Message(
                        MessagePrefix.GAME,
                        true,
                        "Player not found"
                ).sendTo(sender);
                return;
            }
        }

        GameMode newGamemode;
        try {
            newGamemode = GameMode.valueOf(gamemode.toUpperCase());
        } catch (IllegalArgumentException e) {
            new Message(
                    MessagePrefix.GAME,
                    true,
                    "Invalid gamemode"
            ).sendTo(sender);
            return;
        }

        assert targetPlayer != null;
        targetPlayer.setGameMode(newGamemode);
        new Message(
                MessagePrefix.GAME,
                "Gamemode of {} set to {}",
                targetPlayer.getName(),
                newGamemode.name()
        ).sendTo(sender);
        new Message(
                MessagePrefix.GAME,
                "Your gamemode has been set to {}",
                newGamemode.name()
        ).sendTo(targetPlayer);
    }

    @Suggestions("gamemodes")
    public @NotNull Set<String> suggestGamemodes(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return Arrays.stream(GameMode.values())
                     .map(Enum::name)
                     .collect(Collectors.toSet());
    }

    @Suggestions("onlinePlayers")
    public @NotNull Set<String> suggestOnlinePlayers(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return GameUtil.getOnlinePlayers();
    }
}
