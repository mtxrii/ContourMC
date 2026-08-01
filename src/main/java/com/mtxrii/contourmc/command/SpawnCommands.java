package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessageConst;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.mtxrii.contourmc.service.SpawnpointService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@CommandContainer
public final class SpawnCommands {
    @Inject private SpawnpointService spawnpointService;
    @Inject private RankService rankService;

    @Command("setspawn <name>")
    public void setNewSpawn(
            @NotNull final Source sender,
            @Argument("name") final String name
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.SPAWN,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }
        this.rankService.requireRank(MessagePrefix.ENV, Rank.STAFF, player);

        this.spawnpointService.saveNewSpawnpoint(name, player.getLocation());
        new Message(
                MessagePrefix.SPAWN,
                "Spawn {} saved successfully.",
                name
        ).sendTo(player);
    }

    @Command("deletespawn|delspawn <name>")
    public void deleteSpawn(
            @NotNull final Source sender,
            @Argument(value = "name", suggestions = "spawns") final String name
    ) {
        if (sender.source() instanceof Player player) {
            this.rankService.requireRank(MessagePrefix.ENV, Rank.STAFF, player);
        }

        this.spawnpointService.deleteSpawnpoint(name);
        new Message(
                MessagePrefix.SPAWN,
                "Spawn {} deleted successfully.",
                name
        ).sendTo(sender);
    }

    @Command("spawns|listspawns")
    public void listSpawns(
            @NotNull final Source sender
    ) {
        Set<String> spawnpointNames = this.spawnpointService.spawnpoints();
        if (spawnpointNames.isEmpty()) {
            new Message(
                    MessagePrefix.SPAWN,
                    "No spawns found."
            ).sendTo(sender);
            return;
        }
        new Message(
                MessagePrefix.SPAWN,
                "Available spawns: {}.",
                String.join(", ", spawnpointNames)
        ).sendTo(sender);
    }

    @Command("spawn [name]")
    public void tpToSpawn(
            @NotNull final Source sender,
            @Argument(value = "name", suggestions = "spawns") String name
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.SPAWN,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }

        if (name == null) {
            Set<String> spawnpointNames = this.spawnpointService.spawnpoints();
            if (spawnpointNames.isEmpty()) {
                MessageConst.NO_SPAWNS_STORED.sendTo(sender);
                return;
            }

            int idx = ThreadLocalRandom.current().nextInt(spawnpointNames.size());
            Iterator<String> it = spawnpointNames.iterator();
            for (int i = 0; i < idx; i++) {
                it.next();
            }
            name = it.next();
        }

        this.spawnpointService.teleportLivingEntityToSpawnpoint(name, player);
    }

    @Suggestions("spawns")
    public @NotNull Set<String> suggestRanks(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return this.spawnpointService.spawnpoints();
    }
}
