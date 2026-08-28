package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.particle.ParticleSpawn;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class StormCloudTask extends BukkitRunnable {
    private final Player owner;
    private final Location cloudCenter;
    private final double radius;
    private final double damage;
    private int remainingTicks;

    public StormCloudTask(Player owner, Location targetLocation, double radius, double damage, int durationTicks) {
        this.owner = owner;
        this.cloudCenter = targetLocation.clone().add(0.0, 5.0, 0.0);
        this.radius = radius;
        this.damage = damage;
        this.remainingTicks = durationTicks;

        this.cloudCenter.getWorld().playSound(this.cloudCenter, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.5f);
    }

    @Override
    public void run() {
        if (!this.owner.isOnline() || this.remainingTicks <= 0) {
            cancel();
            return;
        }

        this.remainingTicks -= 10;

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
            double x = this.cloudCenter.getX() + Math.cos(angle) * 2.0;
            double z = this.cloudCenter.getZ() + Math.sin(angle) * 2.0;
            Location pLoc = new Location(this.cloudCenter.getWorld(), x, this.cloudCenter.getY(), z);
            new ParticleSpawn(Particle.CAMPFIRE_COSY_SMOKE, this.cloudCenter.getWorld(), pLoc).spawn();
            new ParticleSpawn(Particle.ELECTRIC_SPARK, this.cloudCenter.getWorld(), pLoc).spawn();
        }

        this.cloudCenter.getWorld().playSound(this.cloudCenter, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.3f, 2.0f);

        for (LivingEntity entity : this.cloudCenter.getNearbyLivingEntities(this.radius, 6.0)) {
            if (entity instanceof Player targetPlayer && targetPlayer.equals(this.owner)) {
                continue;
            }
            entity.damage(this.damage, this.owner);
            if (Math.random() < 0.5) {
                entity.getWorld().strikeLightningEffect(entity.getLocation());
            }
        }
    }
}
