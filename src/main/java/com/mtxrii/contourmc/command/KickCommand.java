package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public final class KickCommand {
    @Inject private RankService rankService;

    @Command("kick <player> [reason]")
    public void deleteSpawn(
            @NotNull final Source sender,
            @Argument(value = "name", suggestions = "spawns") final String name
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.MOD, Rank.MEDIATOR, player);
        }
    }
}
