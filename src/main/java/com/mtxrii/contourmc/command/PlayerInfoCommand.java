package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.PlayerRegistryConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SpawnpointService;
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

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@CommandContainer
public final class PlayerInfoCommand {
    @Inject private RankService rankService;
    @Inject private PlayerRegistryService playerRegistryService;
    @Inject private SpawnpointService spawnpointService;

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
                "\nPast Names: {}";
        return new Message(
                MessagePrefix.INFO,
                messageTemplate,
                player.getName(),
                String.valueOf(true),
                TextUtil.formatEnumName(this.rankService.getRank(player.getUniqueId())),
                TextUtil.formatLocation(player.getLocation()),
                this.spawnpointService.getLastSpawnpointForPlayer(player.getUniqueId()),
                healthStr,
                TextUtil.formatInstant(Instant.parse(playerData.firstOnline)),
                this.playerRegistryService.getCurrentKit(player.getUniqueId()),
                formatPastNames(playerData)
        );
    }

    private Message compileMsgOfflinePlayer(String playerName) {
        var offlinePlayerData = this.playerRegistryService.getPlayerDataByName(playerName);
        String messageTemplate = "{}:" +
                "\nOnline: {}" +
                "\nRank: {}" +
                "\nLast Online: {}" +
                "\nLast kit: {}" +
                "\nPast Names: {}";
        return new Message(
                MessagePrefix.INFO,
                messageTemplate,
                offlinePlayerData.name,
                String.valueOf(false),
                TextUtil.formatEnumName(this.rankService.getRank(offlinePlayerData.uniqueId)),
                TextUtil.formatInstant(Instant.parse(offlinePlayerData.lastOnline)),
                offlinePlayerData.currentKit,
                formatPastNames(offlinePlayerData)
        );
    }

    private static String formatPastNames(PlayerRegistryConfiguration.PlayerData playerData) {
        if (playerData == null) {
            return "None";
        }
        return String.join(", ", playerData.pastNames);
    }
}
