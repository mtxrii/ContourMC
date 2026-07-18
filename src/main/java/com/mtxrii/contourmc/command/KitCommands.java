package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
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
public final class KitCommands {
    @Inject
    private KitService kitService;

    @Command("kit list")
    public void listKits(
            @NotNull final Source sender
    ) {
        Set<String> kitNames = this.kitService.kitNames();
        if (kitNames.isEmpty()) {
            new Message(
                    MessagePrefix.KIT,
                    true,
                    "No kits found."
            ).sendTo(sender);
            return;
        }
        new Message(
                MessagePrefix.KIT,
                "Available kits: {}.",
                String.join(", ", kitNames)
        ).sendTo(sender);
    }

    @Command("kit equip <kitName>")
    public void equipKit(
            @NotNull final Source sender,
            @Argument(value = "kitName", suggestions = "kits") final String kitName
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.KIT,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }

        this.kitService.equipKit(kitName, player);
        new Message(
                MessagePrefix.KIT,
                "Kit {} equipped successfully",
                kitName
        ).sendTo(player);
    }

    @Command("kit save <kitName>")
    public void saveNewKit(
            @NotNull final Source sender,
            @Argument("kitName") final String kitName
    ) {
        if (!(sender.source() instanceof Player player)) {
            new Message(
                    MessagePrefix.KIT,
                    true,
                    "You must be a player to run this command."
            ).sendTo(sender);
            return;
        }

        this.kitService.saveNewKit(kitName, player.getInventory());
        new Message(
                MessagePrefix.KIT,
                "Kit {} saved successfully",
                kitName
        ).sendTo(player);
    }

    @Command("kit delete <kitName>")
    public void deleteKit(
            @NotNull final Source sender,
            @Argument(value = "kitName", suggestions = "kits") final String kitName
    ) {
        this.kitService.deleteKit(kitName);
        new Message(
                MessagePrefix.KIT,
                "Kit {} deleted successfully",
                kitName
        ).sendTo(sender);
    }

    @Suggestions("kits")
    public @NotNull Set<String> suggestKit(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return this.kitService.kitNames();
    }
}
