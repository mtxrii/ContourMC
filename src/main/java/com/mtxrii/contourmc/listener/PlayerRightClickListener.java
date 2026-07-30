package com.mtxrii.contourmc.listener;

import com.mtxrii.contourmc.customitem.CustomItem;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/// On player right-click with custom item:
/// - Run custom item effect
@Component
public class PlayerRightClickListener implements Listener {
    private static final Map<String, Map<CustomItem, Date>> COOLDOWN_MAP = new HashMap();

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
            this.runEffectWithCooldown(player, customItem, targetBlock.getLocation());
        }
    }

    private void runEffectWithCooldown(Player player, CustomItem customItem, Location executionLocation) {
        long cooldownSecondsLeft = getCooldownSecondsLeft(player.getName(), customItem);
        if (cooldownSecondsLeft > 0) {
            new Message(
                    MessagePrefix.GAME,
                    true,
                    "Wait {} seconds before using this item again.",
                    String.valueOf(cooldownSecondsLeft)
            ).sendTo(player);
            return;
        }
        customItem.getEffect().execute(executionLocation);

        if (!COOLDOWN_MAP.containsKey(player.getName())) {
            COOLDOWN_MAP.put(player.getName(), new HashMap<>());
        }
        COOLDOWN_MAP.get(player.getName()).put(customItem, new Date());
    }

    private static long getCooldownSecondsLeft(String playerName, CustomItem customItem) {
        if (!COOLDOWN_MAP.containsKey(playerName) || !COOLDOWN_MAP.get(playerName).containsKey(customItem)) {
            return 0;
        }
        Date lastUse = COOLDOWN_MAP.get(playerName).get(customItem);
        long millisecondsSinceLastUse = (new Date()).getTime() - lastUse.getTime();
        long minutesSinceLastUse = TimeUnit.MILLISECONDS.toSeconds(millisecondsSinceLastUse);
        return customItem.getCooldownSeconds() - minutesSinceLastUse;
    }
}
