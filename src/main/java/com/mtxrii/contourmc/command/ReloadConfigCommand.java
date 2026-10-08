package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.ValkeyOptionsClientService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

@CommandContainer
public final class ReloadConfigCommand {
    @Inject private RankService rankService;
    @Inject private ValkeyOptionsClientService valkeyOptionsClientService;

    @Command("reloadConfig")
    @CommandDescription("Reloads configuration options from db")
    public void reloadConfig(@NotNull final Source sender) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.ENV, Rank.STAFF, player);
        }

        try {
            this.valkeyOptionsClientService.reload();
            new Message(MessagePrefix.ENV, "Configuration options reloaded from db").sendTo(sender);
        } catch (RuntimeException exception) {
            new Message(
                    MessagePrefix.ENV,
                    true,
                    "Could not reload configuration options from db"
            ).sendTo(sender);
        }
    }
}
