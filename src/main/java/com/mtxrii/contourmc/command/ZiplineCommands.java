package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.ZiplineService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@CommandContainer
public final class ZiplineCommands {
    @Inject private ZiplineService ziplineService;
    @Inject private RankService rankService;

    @Command("zipline create <name>")
    @CommandDescription("Starts creating a new zipline at your current location")
    public void createZipline(
            @NotNull final Source sender,
            @Argument("name") final String name
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(MessagePrefix.ZIPLINE, true, "You must be a player to run this command.").sendTo(sender);
            return;
        }
        this.rankService.requireRank(MessagePrefix.ZIPLINE, Rank.STAFF, player);

        this.ziplineService.setPendingStart(player.getUniqueId(), player.getLocation().toCenterLocation().subtract(0, 0.5, 0));
        new Message(
                MessagePrefix.ZIPLINE,
                "Zipline start set at your location for '{}'. Now go to the end point and run '{}'",
                name,
                "/zipline finish " + name
        ).sendTo(player);
    }

    @Command("zipline finish <name>")
    @CommandDescription("Finishes creating a zipline using your pending start location")
    public void finishZipline(
            @NotNull final Source sender,
            @Argument("name") final String name
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(MessagePrefix.ZIPLINE, true, "You must be a player to run this command.").sendTo(sender);
            return;
        }
        this.rankService.requireRank(MessagePrefix.ZIPLINE, Rank.STAFF, player);

        Location start = this.ziplineService.getPendingStart(player.getUniqueId());
        if (start == null) {
            new Message(
                    MessagePrefix.ZIPLINE,
                    true,
                    "You have no pending zipline start location. Run {} first.",
                    "/zipline create <name>"
            ).sendTo(player);
            return;
        }

        this.ziplineService.createZipline(name, start, player.getLocation().toCenterLocation().subtract(0, 0.5, 0));
        new Message(
                MessagePrefix.ZIPLINE,
                "Zipline '{}' created successfully.",
                name
        ).sendTo(player);
    }

    @Command("zipline delete <name>")
    @CommandDescription("Deletes an existing zipline")
    public void deleteZipline(
            @NotNull final Source sender,
            @Argument("name") final String name
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.ZIPLINE, Rank.STAFF, player);
        }

        this.ziplineService.deleteZipline(name);
        new Message(
                MessagePrefix.ZIPLINE,
                "Zipline '{}' deleted successfully.",
                name
        ).sendTo(sender);
    }

    @Command("zipline list")
    @Command("listziplines")
    @CommandDescription("Lists all active ziplines")
    public void listZiplines(
            @NotNull final Source sender
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.ZIPLINE, Rank.MEDIATOR, player);
        }

        Set<String> ziplines = this.ziplineService.ziplineNames();
        if (ziplines.isEmpty()) {
            new Message(MessagePrefix.ZIPLINE, "No ziplines found.").sendTo(sender);
            return;
        }
        new Message(MessagePrefix.ZIPLINE, "Active ziplines: {}.", Message.multi(ziplines)).sendTo(sender);
    }
}
