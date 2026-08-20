package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerRegistryService;
import com.mtxrii.contourmc.service.RankService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public class IpCommand {
    @Inject private PlayerRegistryService playerRegistryService;
    @Inject private RankService rankService;

    @Command("ipaddress [player]")
    @CommandDescription("Get a player's IP address")
    public void ipAddress(
            @NotNull final Source sender,
            @Argument("player") String targetPlayer
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.ENV, Rank.MEDIATOR, player);
        }

        if (targetPlayer == null) {
            targetPlayer = sender.source().getName();
        } else {
            targetPlayer = targetPlayer.toLowerCase();
        }
        Player target = Bukkit.getPlayer(targetPlayer);
        if (target == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "Player not found"
            ).sendTo(sender);
        } else {
            new Message(
                    MessagePrefix.MOD,
                    "{}'s IP address is {}",
                    target.getName(),
                    this.playerRegistryService.getPlayerIp(target.getUniqueId())
            ).sendTo(sender);
        }
    }
}
