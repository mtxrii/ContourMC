package com.mtxrii.contourmc.listener;

import com.mtxrii.contourmc.customitem.CustomItem;
import com.mtxrii.contourmc.customitem.CustomItemCooldown;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.HashMap;

/// On player right-click with custom item:
/// - Run custom item effect
@Component
public class PlayerRightClickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        CustomItem customItem = CustomItem.getCustomItem(mainHandItem.getType(), mainHandItem);
        if (customItem == null) {
            return;
        }

        event.setCancelled(true);
        Block targetBlock = player.getTargetBlockExact(350);
        if (targetBlock != null) {
            this.runEffectWithCooldown(player, customItem, targetBlock.getLocation());
            event.setCancelled(true);
        }
    }

    private void runEffectWithCooldown(Player player, CustomItem customItem, Location executionLocation) {
        long cooldownSecondsLeft = CustomItemCooldown.getCooldownSecondsLeft(player.getName(), customItem);
        if (cooldownSecondsLeft > 0) {
            new Message(
                    MessagePrefix.BLANK,
                    true,
                    "Wait {} seconds before using this item again.",
                    String.valueOf(cooldownSecondsLeft)
            ).sendAsActionBar(player);
            return;
        }
        boolean itemWasUsed = customItem.getEffect().execute(executionLocation, player);
        if (!itemWasUsed) {
            return;
        }

        if (!CustomItemCooldown.COOLDOWN_MAP.containsKey(player.getName())) {
            CustomItemCooldown.COOLDOWN_MAP.put(player.getName(), new HashMap<>());
        }
        CustomItemCooldown.COOLDOWN_MAP.get(player.getName()).put(customItem, new Date());
    }
}
