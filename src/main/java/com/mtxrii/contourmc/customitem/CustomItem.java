package com.mtxrii.contourmc.customitem;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.particle.ParticleActionUtil;
import com.mtxrii.contourmc.particle.ParticleSpawn;
import com.mtxrii.contourmc.util.ItemUtil;
import com.mtxrii.contourmc.util.TextUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public enum CustomItem {
    LIGHTNING_STICK(Material.BREEZE_ROD, "Smite Stick", 5, (targetLocation, ignoredUser) -> {
        targetLocation.getWorld().strikeLightningEffect(targetLocation);
    }),

    TNT_PLANTER_SEED(Material.PUMPKIN_SEEDS, "TNT Seeds", 10, (targetLocation, ignoredUser) -> {
        targetLocation.getWorld().spawn(
                targetLocation,
                TNTPrimed.class,
                entity -> {
                    entity.setFuseTicks(40);
                    entity.setYield(4.0f);
                }
        );
    }),

    LASER_POINTER(Material.BLAZE_ROD, "Laser Pointer", (ignoreTargetLocation, player) -> {
        final int LASER_DISTANCE = 128;
        final double LASER_DAMAGE = 2.0;
        Location headLocation = player.getEyeLocation();
        Vector lookDirection = headLocation.getDirection().multiply(0.125);
        for (double i = 0; i < LASER_DISTANCE; i++) {
            headLocation.add(lookDirection);
            new ParticleSpawn(Particle.FLAME, player.getWorld(), headLocation).spawn();
            new ParticleSpawn(Particle.CLOUD, player.getWorld(), headLocation).spawn();

            Material blockTypeAtLaser = headLocation.getBlock().getType();
            if (!blockTypeAtLaser.isAir()) {
                break;
            }

            if (ParticleActionUtil.damageEntitiesInArea(headLocation, player, LASER_DAMAGE)) {
                break;
            }
        }
    }),

    AURA_COLLECTOR(Material.DIAMOND_PICKAXE, "Aura Collector", (ignoreTargetLocation, player) -> {
        final double PULL_RADIUS = 10.0;
        final double PULL_STRENGTH = 0.35;
        Location playerLocation = player.getLocation();
        for (org.bukkit.entity.Player targetPlayer : playerLocation.getNearbyPlayers(PULL_RADIUS)) {
            if (targetPlayer.equals(player)) {
                continue;
            }

            Vector pullDirection = playerLocation.toVector()
                                                 .subtract(targetPlayer.getLocation().toVector())
                                                 .normalize()
                                                 .multiply(PULL_STRENGTH);
            targetPlayer.setVelocity(targetPlayer.getVelocity().add(pullDirection));
        }
    }),

    DEBUG_HOE(Material.COPPER_HOE, "Debug Hoe", (targetLocation, ignoredUser) -> {
        new Message(MessagePrefix.GAME, "Pointing at {}", TextUtil.formatLocation(targetLocation)).sendToAll();
    });

    private final Material material;
    private final String itemDisplayName;
    @Getter private final int cooldownSeconds;
    @Getter private final CustomItemEffect effect;

    CustomItem(Material material, String itemDisplayName, int cooldownSeconds, CustomItemEffect effect) {
        this.material = material;
        this.itemDisplayName = itemDisplayName;
        this.cooldownSeconds = cooldownSeconds;
        this.effect = effect;
    }

    CustomItem(Material material, String itemDisplayName, CustomItemEffect effect) {
        this.material = material;
        this.itemDisplayName = itemDisplayName;
        this.cooldownSeconds = 0;
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
