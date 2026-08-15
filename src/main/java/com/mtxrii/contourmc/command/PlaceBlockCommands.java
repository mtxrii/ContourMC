package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public final class PlaceBlockCommands {
    @Inject private RankService rankService;

    @Command("placeBlockUnder|placeUnder|pu")
    @CommandDescription("Places a block under your feet. Even if you're in the air")
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

    @Command("placeBlockAt|placeAt|pa")
    @CommandDescription("Places a block at the location your cursor is pointing at")
    public void placeBlockAt(
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

        Block targetBlock = player.getTargetBlockExact(350);
        if (targetBlock == null) {
            new Message(MessagePrefix.BUILD, "No blocks in immediate range of sight.").sendTo(player);
        } else {
            targetBlock = targetBlock.getLocation().add(0, 1, 0).getBlock();
            targetBlock.setType(Material.GLASS);
            new Message(
                    MessagePrefix.BUILD,
                    "Block placed at {}",
                    TextUtil.formatLocation(targetBlock.getLocation())
            ).sendTo(player);
        }
    }
}
