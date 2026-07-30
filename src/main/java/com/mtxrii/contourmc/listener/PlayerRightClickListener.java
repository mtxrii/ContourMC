package com.mtxrii.contourmc.listener;

import com.mtxrii.contourmc.customitem.CustomItem;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/// On player right-click with custom item:
/// - Run custom item effect
@Component
public class PlayerRightClickListener implements Listener {

    @EventHandler
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
            customItem.getEffect().execute(targetBlock.getLocation());
        }
    }
}
