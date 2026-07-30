package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public final class ClearItemNameFormattingCommand {
    private static final Message COMMAND_COMPLETED_MSG = new Message(
            MessagePrefix.GAME,
            "Main hand item name formatting cleared."
    );

    @Inject private RankService rankService;

    @Command("clearItemNameFormatting")
    public void clearItemNameFormatting(@NotNull final Source sender) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.GAME,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }
        this.rankService.requireRank(MessagePrefix.GAME, Rank.STAFF, player);

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            COMMAND_COMPLETED_MSG.sendTo(player);
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            COMMAND_COMPLETED_MSG.sendTo(player);
            return;
        }

        Component displayName = meta.displayName();
        if (displayName == null) {
            COMMAND_COMPLETED_MSG.sendTo(player);
            return;
        }

        String plainText = PlainTextComponentSerializer.plainText().serialize(displayName);
        meta.displayName(Component.text(plainText).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        COMMAND_COMPLETED_MSG.sendTo(player);
    }
}
