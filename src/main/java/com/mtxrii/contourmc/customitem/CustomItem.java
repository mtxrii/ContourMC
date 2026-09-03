package com.mtxrii.contourmc.customitem;

import com.mtxrii.contourmc.ContourMCPlugin;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.particle.ParticleActionUtil;
import com.mtxrii.contourmc.particle.ParticleSpawn;
import com.mtxrii.contourmc.runnabletask.GroundSlamTask;
import com.mtxrii.contourmc.runnabletask.StormCloudTask;
import com.mtxrii.contourmc.runnabletask.ZiplineTask;
import com.mtxrii.contourmc.util.ItemUtil;
import com.mtxrii.contourmc.util.TextUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public enum CustomItem {
    LIGHTNING_STICK(Material.BREEZE_ROD, "Smite Stick", 5, (targetLocation, ignoredUser) -> {
        targetLocation.getWorld().strikeLightningEffect(targetLocation);
        return true;
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
        return true;
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
        return true;
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
        return true;
    }),

    GRAPPLING_HOOK(Material.TRIPWIRE_HOOK, "Grappling Hook", 3, (ignoreTargetLocation, player) -> {
        final double MAX_DISTANCE = 30.0;
        final double MIN_DISTANCE = 9.0;
        final double SPEED = 1.5;

        Location start = player.getEyeLocation();
        Vector direction = start.getDirection();
        Location target = start.clone();

        // Raycast to find anchor point
        boolean hit = false;
        for (double d = 0; d < MAX_DISTANCE; d += 0.5) {
            target.add(direction.clone().multiply(0.5));
            if (!target.getBlock().getType().isAir()) {
                hit = true;
                break;
            }
        }

        if (!hit || target.distanceSquared(start) < MIN_DISTANCE) {
            return false; // Too close or no valid landing block
        }

        new ZiplineTask(player, start, target, SPEED).runTaskTimer(ContourMCPlugin.pluginClass, 0L, 1L);
        return true;
    }),

    GROUND_SLAM(Material.NETHERITE_AXE, "Ground Slammer", 8, (ignoreTargetLocation, player) -> {
        new GroundSlamTask(player, 6.0, 5.0).runTaskTimer(ContourMCPlugin.pluginClass, 0L, 1L);
        return true;
    }),

    STORM_CALLER(Material.PRISMARINE_SHARD, "Storm Caller", 12, (targetLocation, player) -> {
        final double DAMAGE = 2.0;
        final double RADIUS = 4.0;
        final int DURATION_SECONDS = 5;
        if (targetLocation == null) {
            Block targetBlock = player.getTargetBlockExact(32);
            if (targetBlock != null) {
                targetLocation = targetBlock.getLocation();
            } else {
                targetLocation = player.getLocation();
            }
        }
        new StormCloudTask(
                player,
                targetLocation,
                RADIUS,
                DAMAGE,
                DURATION_SECONDS * 20
        ).runTaskTimer(ContourMCPlugin.pluginClass, 0L, 10L);
        return true;
    }),

    VAMPIRE_BLADE(Material.IRON_SWORD, "Vampire Blade", 10, (ignoreTargetLocation, player) -> {
        final double RANGE = 3.0;
        final double DAMAGE = 4.0;
        final double HEAL = 2.0;
        boolean hitEntity = false;
        for (double d = 0; d < RANGE; d += 0.5) {
             Location checkLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(d));
             java.util.Collection<org.bukkit.entity.LivingEntity> entities = checkLoc.getNearbyLivingEntities(1.0);
             for (org.bukkit.entity.LivingEntity entity : entities) {
                 if (entity.equals(player)) continue;
                 entity.damage(DAMAGE, player);
                 player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + HEAL));
                 hitEntity = true;
                 break;
             }
             if (hitEntity) break;
        }
        return hitEntity;
    }),

    DEBUG_HOE(Material.COPPER_HOE, "Debug Hoe", (targetLocation, player) -> {
        Message message = new Message(MessagePrefix.GAME, "Pointing at {}", TextUtil.formatLocation(targetLocation));
        message.sendTo(player);
        message.sendAsActionBar(player);
        return true;
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
