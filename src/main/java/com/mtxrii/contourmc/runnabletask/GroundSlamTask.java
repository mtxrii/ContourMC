package com.mtxrii.contourmc.runnabletask;

import com.mtxrii.contourmc.particle.ParticleSpawn;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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

        Vector leapVector = player.getEyeLocation().getDirection().setY(0).normalize().multiply(0.6).setY(1.2);
        player.setVelocity(leapVector);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 0.5f);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead()) {
            cancel();
            return;
        }

        ticksInAir++;

        if (ticksInAir > 2) {
            new ParticleSpawn(Particle.CLOUD, player.getWorld(), player.getLocation()).spawn();
        }

        if (ticksInAir > 5 && (player.isOnGround() || player.getLocation().getBlock().getType().isSolid() || ticksInAir > 100)) {
            // @TODO: Implement ground slam logic
            cancel();
        }
    }
}
