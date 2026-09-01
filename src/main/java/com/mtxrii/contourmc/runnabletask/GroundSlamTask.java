package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.particle.ParticleSpawn;
import com.mtxrii.contourmc.util.GameUtil;
import com.mtxrii.contourmc.ContourMCPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class GroundSlamTask extends BukkitRunnable {
    private final Player player;
    private final double damage;
    private final double radius;
    private int ticksInAir;

    public GroundSlamTask(Player player, double damage, double radius) {
        this.player = player;
        this.damage = damage;
        this.radius = radius;
        this.ticksInAir = 0;

        player.setMetadata("GROUND_SLAM", new FixedMetadataValue(ContourMCPlugin.pluginClass, true));

        Vector leapVector = player.getEyeLocation().getDirection().setY(0).normalize().multiply(0.6).setY(1.2);
        player.setVelocity(leapVector);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 0.5f);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead()) {
            cleanup();
            cancel();
            return;
        }

        ticksInAir++;

        // Increase fall speed
        if (player.getVelocity().getY() < -0.1) {
            player.setVelocity(player.getVelocity().add(new Vector(0, -0.3, 0)));
        }

        if (ticksInAir > 2) {
            new ParticleSpawn(Particle.CLOUD, player.getWorld(), player.getLocation()).spawn();
        }

        if (ticksInAir > 5 && (GameUtil.isOnGround(player, false) || ticksInAir > 100)) {
            executeSlam();
            cleanup();
            cancel();
        }
    }

    private void executeSlam() {
        Location impactLoc = player.getLocation();
        impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 0.8f);
        impactLoc.getWorld().playSound(impactLoc, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);

        for (double d = 0; d < Math.PI * 2; d += Math.PI / 8) {
            double x = impactLoc.getX() + Math.cos(d) * 2.0;
            double z = impactLoc.getZ() + Math.sin(d) * 2.0;
            Location particleLoc = new Location(impactLoc.getWorld(), x, impactLoc.getY(), z);
            new ParticleSpawn(Particle.EXPLOSION, impactLoc.getWorld(), particleLoc).spawn();
            new ParticleSpawn(Particle.CAMPFIRE_COSY_SMOKE, impactLoc.getWorld(), particleLoc).spawn();
        }

        for (LivingEntity entity : impactLoc.getNearbyLivingEntities(radius)) {
            if (entity instanceof Player targetPlayer && targetPlayer.equals(player)) {
                continue;
            }
            entity.damage(damage, player);
            Vector pushVector = entity.getLocation().toVector().subtract(impactLoc.toVector()).normalize().multiply(1.2).setY(0.6);
            entity.setVelocity(pushVector);
        }
    }

    private void cleanup() {
        if (player.hasMetadata("GROUND_SLAM")) {
            player.removeMetadata("GROUND_SLAM", ContourMCPlugin.pluginClass);
        }
    }
}
