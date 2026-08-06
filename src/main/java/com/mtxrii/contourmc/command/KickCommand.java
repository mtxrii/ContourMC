package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SanctionService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public final class KickCommand {
    @Inject private RankService rankService;
    @Inject private SanctionService sanctionService;

    @Command("kick <player> [reason]")
    public void kickPlayer(
            @NotNull final Source sender,
            @Argument(value = "player") final String playerName,
            @Argument(value = "reason") final String reason
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.MOD, Rank.MEDIATOR, player);
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            new Message(
                    MessagePrefix.MOD,
                    true,
                    "Player not found"
            ).sendTo(sender);
            return;
        }

        if (reason != null) {
            this.sanctionService.kick(target, reason);
        } else {
            this.sanctionService.kick(target);
        }
    }
}
