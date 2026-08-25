package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.particle.ParticleSpawn;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class ZiplineTask extends BukkitRunnable {
    private final Player player;
    private final Location start;
    private final Location target;
    private final double speed;
    private final double totalDistance;
    private double progress;

    public ZiplineTask(Player player, Location start, Location target, double speed) {
        this.player = player;
        this.start = start;
        this.target = target;
        this.speed = speed;
        this.totalDistance = start.distance(target);
        this.progress = 0.0;
        player.setGravity(false);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || progress >= totalDistance) {
            cancel();
            return;
        }

        progress += speed;
        double ratio = Math.min(1.0, progress / totalDistance);

        // @TODO: Move linear interpolation logic to separate method in util class
        double x = start.getX() + (target.getX() - start.getX()) * ratio;
        double y = start.getY() + (target.getY() - start.getY()) * ratio;
        double z = start.getZ() + (target.getZ() - start.getZ()) * ratio;
        Location currentLoc = new Location(start.getWorld(), x, y, z);
        currentLoc.setDirection(target.clone().subtract(start).toVector());

        player.teleport(currentLoc);
        player.setVelocity(new Vector(0, 0, 0)); // Lock velocity during ride

        // Spawn cable particles along the path
        new ParticleSpawn(Particle.ELECTRIC_SPARK, player.getWorld(), currentLoc).spawn();

        if (ratio >= 1.0) {
            cancel();
        }
    }
}
