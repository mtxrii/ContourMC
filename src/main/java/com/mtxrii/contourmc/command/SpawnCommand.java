package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.exception.SpawnpointArgumentException;
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
            sender.source().sendMessage("[Error] You must be a player to run this command.");
            return;
        }

        try {
            this.spawnpointService.saveNewSpawnpoint(name, player.getLocation());
            player.sendMessage("[Spawn] Spawn '" + name + "' saved successfully.");
        } catch (SpawnpointArgumentException e) {
            player.sendMessage(e.getMessage());
        }
    }

    @Command("deletespawn|delspawn <name>")
    public void deleteSpawn(
            @NotNull final Source sender,
            @Argument("name") final String name
    ) {
        try {
            this.spawnpointService.deleteSpawnpoint(name);
            sender.source().sendMessage("[Spawn] Spawn '" + name + "' deleted successfully.");
        } catch (SpawnpointArgumentException e) {
            sender.source().sendMessage(e.getMessage());
        }
    }

    @Command("spawns|listspawns")
    public void listSpawns(
            @NotNull final Source sender
    ) {
        Set<String> spawnpointNames = this.spawnpointService.spawnpoints();
        if (spawnpointNames.isEmpty()) {
            sender.source().sendMessage("[Spawn] No spawns found.");
            return;
        }
        sender.source().sendMessage("[Spawn] Available spawns: " + String.join(", ", spawnpointNames));
    }

    @Command("spawn [name]")
    public void tpToSpawn(
            @NotNull final Source sender,
            @Argument("name") String name
    ) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage("[Error] You must be a player to run this command.");
            return;
        }

        if (name == null) {
            Set<String> spawnpointNames = this.spawnpointService.spawnpoints();
            if (spawnpointNames.isEmpty()) {
                sender.source().sendMessage("[Spawn] No spawns available to teleport to.");
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
            player.sendMessage("[Spawn] Teleported to " + name);
        } catch (SpawnpointArgumentException e) {
            player.sendMessage(e.getMessage());
        }
    }
}
