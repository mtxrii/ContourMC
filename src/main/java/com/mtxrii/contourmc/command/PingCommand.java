package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public class PingCommand {
    @Inject private RankService rankService;

    @Command("ping")
    @CommandDescription("Gets your ping in ms")
    public void ping(
            @NotNull final Source sender
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.GAME,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }
        this.rankService.requireRank(MessagePrefix.ENV, Rank.PLAYER, player);

        new Message(
                MessagePrefix.GAME,
                "Your ping is {}",
                String.valueOf(player.getPing()) + "ms"
        ).sendTo(player);
    }
}
