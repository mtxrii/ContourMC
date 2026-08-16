package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.exception.CommandArgumentException;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.KitService;
import com.sxtanna.platform.archetype.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

@Component
public class PlayerItemFrameClickListener implements Listener {
    private final KitService kitService;

    @Inject
    public PlayerItemFrameClickListener(KitService kitService) {
        this.kitService = kitService;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractItemFrame(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) {
            return;
        }

        net.kyori.adventure.text.Component customName = itemFrame.customName();
        if (customName == null) {
            return;
        }

        String kitName = PlainTextComponentSerializer.plainText().serialize(customName).trim();
        if (kitName.isEmpty()) {
            return;
        }

        String matchedKit = findKit(kitName);
        if (matchedKit == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        try {
            this.kitService.equipKit(matchedKit, player);
        } catch (CommandArgumentException e) {
            new Message(
                    MessagePrefix.KIT,
                    true,
                    "This kit is currently unavailable"
            ).sendTo(player);
            return;
        }

        new Message(
                MessagePrefix.KIT,
                "Kit {} equipped successfully",
                matchedKit
        ).sendTo(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAttackItemFrame(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) {
            return;
        }

        net.kyori.adventure.text.Component customName = itemFrame.customName();
        if (customName == null) {
            return;
        }

        String kitName = PlainTextComponentSerializer.plainText().serialize(customName).trim();
        if (findKit(kitName) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreakItemFrame(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) {
            return;
        }

        net.kyori.adventure.text.Component customName = itemFrame.customName();
        if (customName == null) {
            return;
        }

        String kitName = PlainTextComponentSerializer.plainText().serialize(customName).trim();
        if (findKit(kitName) != null) {
            event.setCancelled(true);
        }
    }

    private String findKit(String name) {
        for (String kit : this.kitService.kitNames()) {
            if (kit.equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }
}
