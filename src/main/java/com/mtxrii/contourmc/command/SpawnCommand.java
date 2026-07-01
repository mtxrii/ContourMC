package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.exception.CommandArgumentException;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.SpawnpointService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@CommandContainer
public final class SpawnCommand {
    @Inject private SpawnpointService spawnpointService;

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

        try {
            this.spawnpointService.saveNewSpawnpoint(name, player.getLocation());
            new Message(
                    MessagePrefix.SPAWN,
                    "Spawn {} saved successfully.",
                    name
            ).sendTo(player);
        } catch (CommandArgumentException e) {
            e.sendTo(player);
        }
    }

    @Command("deletespawn|delspawn <name>")
    public void deleteSpawn(
            @NotNull final Source sender,
            @Argument("name") final String name
    ) {
        try {
            this.spawnpointService.deleteSpawnpoint(name);
            new Message(
                    MessagePrefix.SPAWN,
                    "Spawn {} deleted successfully.",
                    name
            ).sendTo(sender);
        } catch (CommandArgumentException e) {
            e.sendTo(sender);
        }
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
            @Argument("name") String name
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
                new Message(
                        MessagePrefix.SPAWN,
                        true,
                        "No spawns available to teleport to."
                ).sendTo(sender);
                return;
            }

            int idx = ThreadLocalRandom.current().nextInt(spawnpointNames.size());
            Iterator<String> it = spawnpointNames.iterator();
            for (int i = 0; i < idx; i++) {
                it.next();
            }
            name = it.next();
        }

        try {
            this.spawnpointService.teleportLivingEntityToSpawnpoint(name, player);
            new Message(
                    MessagePrefix.SPAWN,
                    "Teleported to {}.",
                    name
            ).sendTo(player);
        } catch (CommandArgumentException e) {
            e.sendTo(player);
        }
    }
}
