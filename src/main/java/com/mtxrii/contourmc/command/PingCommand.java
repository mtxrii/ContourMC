package com.mtxrii.contourmc.command;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public class PingCommand {

    @Command("ping")
    @CommandDescription("")
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

        new Message(
                MessagePrefix.GAME,
                "Your ping is {} ms",
                String.valueOf(player.getPing())
        ).sendTo(player);
    }
}
