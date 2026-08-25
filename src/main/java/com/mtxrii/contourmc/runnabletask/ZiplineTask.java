package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.particle.ParticleSpawn;
import com.mtxrii.contourmc.util.LocUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class ZiplineTask extends BukkitRunnable {
    private final Player player;
    private final Location start;
    private final Location target;
    private final double speed;
    private final double totalDistance;
    private final boolean originalGravityState;
    private double progress;

    public ZiplineTask(Player player, Location start, Location target, double speed) {
        this.player = player;
        this.start = start;
        this.target = target;
        this.speed = speed;
        this.totalDistance = start.distance(target);
        this.originalGravityState = player.hasGravity();
        this.progress = 0.0;
        player.setGravity(false);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || progress >= totalDistance) {
            cleanup();
            cancel();
            return;
        }

        progress += speed;
        double ratio = Math.min(1.0, progress / totalDistance);
        Location currentLoc = LocUtil.lerp(start, target, ratio);
        currentLoc.setDirection(target.clone().subtract(start).toVector());

        player.teleport(currentLoc);
        player.setVelocity(new Vector(0, 0, 0)); // Lock velocity during ride

        // Spawn cable particles along the path
        new ParticleSpawn(Particle.ELECTRIC_SPARK, player.getWorld(), currentLoc).spawn();

        if (ratio >= 1.0) {
            cleanup();
            cancel();
        }
    }

    private void cleanup() {
        player.setGravity(originalGravityState);
        player.setVelocity(target.clone().subtract(start).toVector().normalize().multiply(0.8)); // Gentle forward boost on exit
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_LEAD_BREAK, 0.5f, 1.2f);
    }
}
