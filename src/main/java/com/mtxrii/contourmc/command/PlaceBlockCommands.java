package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public final class PlaceBlockCommands {
    @Inject private RankService rankService;

    @Command("placeBlockUnder|placeUnder|pu")
    public void placeBlockUnder(
            @NotNull final Source sender
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.BUILD,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }
        this.rankService.requireRank(MessagePrefix.ENV, Rank.STAFF, player);

        Block blockAtFeet = player.getLocation().subtract(0, 1, 0).getBlock();
        if (blockAtFeet.getType().isAir()) {
            blockAtFeet.setType(Material.GLASS);
            new Message(MessagePrefix.BUILD, "Block placed under you.").sendTo(player);
        } else {
            new Message(MessagePrefix.BUILD, "You are already standing on a block.").sendTo(player);
        }
    }
}
