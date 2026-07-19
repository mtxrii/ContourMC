package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.util.SearchUtil;
import com.mtxrii.contourmc.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

@CommandContainer
public final class PlayerInfoCommand {
    @Inject private RankService rankService;
    @Inject private PlayerRegistryService playerRegistryService;

    @Command("playerinfo <player>")
    public void playerInfo(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        Message response;
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            String offlinePlayerName = SearchUtil.findClosestMatch(
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

            response = this.compileMsgOfflinePlayer(offlinePlayerName);
        } else {
            response = this.compileMsgOnlinePlayer(player);
        }
        response.sendTo(sender);
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

    private Message compileMsgOnlinePlayer(Player player) {
        String locationStr = TextUtil.formatLocation(player.getLocation());
        // @TODO: Truncate player.getHealth() double to 1 decimal place
        String healthStr = '(' + String.valueOf(player.getHealth()) + '/' + player.getAttribute(Attribute.MAX_HEALTH).getValue() + ')';
        // @TODO: Save players' kits and include them here
        var playerData = this.playerRegistryService.getPlayerDataByName(player.getName());

        String messageTemplate = "{}:" +
                "\nOnline: {}" +
                "\nRank: {}" +
                "\nLocation: {}" +
                "\nHealth: {}" +
                "\nJoin Date: {}" +
                "\nPast Names: {}";
        return new Message(
                MessagePrefix.INFO,
                messageTemplate,
                player.getName(),
                String.valueOf(true),
                TextUtil.formatEnumName(this.rankService.getRank(player.getUniqueId())),
                locationStr,
                healthStr,
                TextUtil.formatInstant(playerData.firstOnline),
                // @TODO: Make util method for auto-adding array's length of template param tokens (See RankCommand.ranks)
                playerData.pastNames.isEmpty() ? "None" : String.join(", ", playerData.pastNames)
        );
    }

    private Message compileMsgOfflinePlayer(String playerName) {
        var offlinePlayerData = this.playerRegistryService.getPlayerDataByName(playerName);
        String messageTemplate = "{}:" +
                "\nOnline: {}" +
                "\nRank: {}" +
                "\nLastOnline: {}" +
                "\nPast Names: {}";
        return new Message(
                MessagePrefix.INFO,
                messageTemplate,
                offlinePlayerData.name,
                String.valueOf(false),
                TextUtil.formatEnumName(this.rankService.getRank(offlinePlayerData.uniqueId)),
                TextUtil.formatInstant(offlinePlayerData.lastOnline),
                offlinePlayerData.pastNames.isEmpty() ? "None" : String.join(", ", offlinePlayerData.pastNames)
        );
    }
}
