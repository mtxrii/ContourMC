package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.exception.KitArgumentException;
import com.mtxrii.contourmc.service.KitService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@CommandContainer
public final class KitCommand {
    @Inject
    private KitService kitService;

    @Command("kit list")
    public void listKits(
            @NotNull final Source sender
    ) {
        Set<String> kitNames = this.kitService.kitNames();
        if (kitNames.isEmpty()) {
            sender.source().sendMessage("[Kit] No kits found.");
            return;
        }
        sender.source().sendMessage("[Kit] Available kits: " + String.join(", ", kitNames));
    }

    @Command("kit equip <kitName>")
    public void equipKit(
            @NotNull final Source sender,
            @Argument(value = "kitName", suggestions = "kits") final String kitName
    ) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage("[Error] You must be a player to run this command.");
            return;
        }

        try {
            this.kitService.equipKit(kitName, player);
            player.sendMessage("[Kit] Kit '" + kitName + "' equipped successfully.");
        } catch (KitArgumentException e) {
            player.sendMessage(e.getMessage());
        }
    }

    @Command("kit save <kitName>")
    public void saveNewKit(
            @NotNull final Source sender,
            @Argument("kitName") final String kitName
    ) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage("[Error] You must be a player to run this command.");
            return;
        }

        try {
            this.kitService.saveNewKit(kitName, player.getInventory());
            player.sendMessage("[Kit] Kit '" + kitName + "' saved successfully.");
        } catch (KitArgumentException e) {
            player.sendMessage(e.getMessage());
        }
    }

    @Command("kit delete <kitName>")
    public void deleteKit(
            @NotNull final Source sender,
            @Argument(value = "kitName", suggestions = "kits") final String kitName
    ) {
        try {
            this.kitService.deleteKit(kitName);
            sender.source().sendMessage("[Kit] Kit '" + kitName + "' deleted successfully.");
        } catch (KitArgumentException e) {
            sender.source().sendMessage(e.getMessage());
        }
    }

    @Suggestions("kits")
    public @NotNull Set<String> suggestKit(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return this.kitService.kitNames();
    }
}
