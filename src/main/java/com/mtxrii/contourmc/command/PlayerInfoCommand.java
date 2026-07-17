package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
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

    @Command("playerinfo <player>")
    public void setRank(
            @NotNull final Source sender,
            @Argument(value = "player", suggestions = "onlinePlayers") final String playerName
    ) {
        // @TODO: Add support for offline players
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            new Message(
                    MessagePrefix.INFO,
                    true,
                    "Player not found"
            ).sendTo(sender);
            return;
        }

        String locationStr = TextUtil.formatLocation(player.getLocation());
        // @TODO: Truncate player.getHealth() double to 1 decimal place
        String healthStr = '(' + String.valueOf(player.getHealth()) + '/' + player.getAttribute(Attribute.MAX_HEALTH).getValue() + ')';
        // @TODO: Save players' kits and include them here

        new Message(
                MessagePrefix.INFO,
                "{}:\nRank: {}\nLocation: {}\nHealth: {}",
                player.getName(),
                // @TODO: Fix ranks not updating automatically
                TextUtil.formatEnumName(this.rankService.getRank(player.getUniqueId())),
                locationStr,
                healthStr
        ).sendTo(sender);
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
}
