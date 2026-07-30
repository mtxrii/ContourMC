package com.mtxrii.contourmc.customitem;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.util.ItemUtil;
import com.mtxrii.contourmc.util.TextUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum CustomItem {
    LIGHTNING_STICK(Material.BREEZE_ROD, "Smite Stick", targetLocation -> {
        targetLocation.getWorld().strikeLightningEffect(targetLocation);
    }),
    DEBUG_HOE(Material.COPPER_HOE, "Debug Hoe", targetLocation -> {
        new Message(MessagePrefix.GAME, "Pointing at {}", TextUtil.formatLocation(targetLocation)).sendToAll();
    });

    private final Material material;
    private final String itemDisplayName;
    @Getter private final CustomItemEffect effect;

    CustomItem(Material material, String itemDisplayName, CustomItemEffect effect) {
        this.material = material;
        this.itemDisplayName = itemDisplayName;
        this.effect = effect;
    }

    public static CustomItem getCustomItem(Material material, ItemStack itemHeld) {
        for (CustomItem item : CustomItem.values()) {
            if (material == item.material && ItemUtil.hasExactPlainName(itemHeld, item.itemDisplayName)) {
                return item;
            }
        }
        return null;
    }
}
