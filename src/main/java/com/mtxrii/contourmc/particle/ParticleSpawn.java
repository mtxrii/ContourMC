package com.mtxrii.contourmc.particle;

import com.mtxrii.contourmc.ContourMCPlugin;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a particle spawn.
 * @source <a href="https://github.com/BloomMC/BloomMC-core/blob/main/src/main/java/com/mtxrii/bloom/core/ParticleSpawn.java">BloomMC-core ParticleSpawn.java</a>
 */
@Slf4j
public class ParticleSpawn {
    private static final int MAX_PARTICLES = 20;

    protected final Particle particleType;
    protected final boolean forOnePlayerOnly;
    protected final Player player;
    protected final World world;
    protected final int count;
    protected final boolean spawnAtExactLocation;
    protected final double spread;
    @Setter protected Location location;

    /**
     * Spawn multiple particles at a general location visible to a single player
     * @param particleType {@link org.bukkit.Particle} type to spawn
     * @param forPlayer Player to display these particles to
     * @param location Location for particles to originate from. If given a block location, will be auto centered.
     * @param count Number of particles to spawn. Must be between 1 and {@value MAX_PARTICLES}, but over 5 not recommended.
     * @param spread Distance particles can appear in. Must be between 0.1 and 2.5.
     */
    public ParticleSpawn(Particle particleType, Player forPlayer, Location location, int count, double spread) {
        this.particleType = particleType;
        this.location = location;
        this.forOnePlayerOnly = true;
        this.player = forPlayer;
        this.world = null;
        this.count = count;
        this.spawnAtExactLocation = false;
        this.spread = spread;
        this.validateConstructorValues();
    }

    /**
     * Spawn one particle at a precise location visible to a single player
     * @param particleType {@link org.bukkit.Particle} type to spawn
     * @param forPlayer Player to display this particle to
     * @param location Location for particle to originate from
     */
    public ParticleSpawn(Particle particleType, Player forPlayer, Location location) {
        this.particleType = particleType;
        this.location = location;
        this.forOnePlayerOnly = true;
        this.player = forPlayer;
        this.world = null;
        this.count = 1;
        this.spawnAtExactLocation = true;
        this.spread = 1.0;
        this.validateConstructorValues();
    }

    /**
     * Spawn multiple particles at a general location visible to all players
     * @param particleType {@link org.bukkit.Particle} type to spawn
     * @param forAllPlayersInWorld World to spawn these particles in
     * @param location Location for particles to originate from. If given a block location, it will be auto-centered.
     * @param count Number of particles to spawn. Must be between 1 and {@value MAX_PARTICLES}, but over 5 not recommended.
     * @param spread Distance particles can appear in. Must be between 0.1 and 2.5.
     */
    public ParticleSpawn(Particle particleType, World forAllPlayersInWorld, Location location, int count, double spread) {
        this.particleType = particleType;
        this.location = location;
        this.forOnePlayerOnly = false;
        this.player = null;
        this.world = forAllPlayersInWorld;
        this.count = count;
        this.spawnAtExactLocation = false;
        this.spread = spread;
        this.validateConstructorValues();
    }

    /**
     * Spawn one particle at a precise location visible to all players
     * @param particleType {@link org.bukkit.Particle} type to spawn
     * @param forAllPlayersInWorld World to spawn this particle in
     * @param location Location for particle to originate from
     */
    public ParticleSpawn(Particle particleType, World forAllPlayersInWorld, Location location) {
        this.particleType = particleType;
        this.location = location;
        this.forOnePlayerOnly = false;
        this.player = null;
        this.world = forAllPlayersInWorld;
        this.count = 1;
        this.spawnAtExactLocation = true;
        this.spread = 1.0;
        this.validateConstructorValues();
    }

    /**
     * Validates args passed to ParticleSpawn during creation.
     */
    private void validateConstructorValues() {
        if (this.count < 1 || this.count > MAX_PARTICLES) {
            throw new IllegalArgumentException(String.format(
                    "ParticleSpawn count must be between 1 and %d. Could not instantiate ParticleSpawn with count: %d",
                    MAX_PARTICLES,
                    this.count
            ));
        }
        if (this.spread < 0.1 || this.spread > 2.5) {
            throw new IllegalArgumentException(
                    "ParticleSpawn spread must be between 0.1 and 2.5. " +
                            "Could not instantiate ParticleSpawn with spread: " + this.spread
            );
        }
    }

    /**
     * Spawns this {@link com.mtxrii.contourmc.particle.ParticleSpawn} accordingly all at once.
     */
    public void spawn() {
        for (int i = 0; i < this.count; i++) {
            this.spawnParticle();
        }
    }

    /**
     * Spawns this {@link com.mtxrii.contourmc.particle.ParticleSpawn} accordingly with a delay after each particle.
     * @param secondsDelay Delay between particles in seconds
     */
    public void spawnWithDelay(double secondsDelay) {
        long ticksDelay = Math.round(secondsDelay * 20);
        new BukkitRunnable() {
            private int particlesLeft = count;

            @Override
            public void run() {
                if (particlesLeft <= 1) {
                    this.cancel();
                }
                spawnParticle();
                particlesLeft--;
            }
        }.runTaskTimer(ContourMCPlugin.pluginClass, 0L, ticksDelay);
    }

    /**
     * Spawns this {@link com.mtxrii.contourmc.particle.ParticleSpawn} as if {@code spawn()} was called once every
     * {@code secondsDelay} for a total of {@code times} times.
     * Spawns {@code count} number of particles at every iteration
     * @param secondsDelay Delay between particle spawns in seconds
     * @param times Number of times to spawn these particles
     */
    public void spawnWithDelay(double secondsDelay, int times) {
        long ticksDelay = Math.round(secondsDelay * 20);
        new BukkitRunnable() {
            private int timesLeft = times;

            @Override
            public void run() {
                if (timesLeft <= 1) {
                    this.cancel();
                }
                spawn();
                timesLeft--;
            }
        }.runTaskTimer(ContourMCPlugin.pluginClass, 0L, ticksDelay);
    }

    protected void spawnParticle() {
        if (this.particleType.getDataType() != Void.class) {
            log.warn(
                    "Cannot spawn particle of type '{}' as it requires a data type, which is not supported by " +
                            "ParticleSpawn. Please use a subclass of particleWithDataSpawn for this.",
                    this.particleType.name()
            );
            if (this.forOnePlayerOnly) {
                assert this.player != null;
                new Message(
                        MessagePrefix.GAME,
                        true,
                        "Feature is momentarily unavailable. Please try again later."
                ).sendTo(this.player);
            }
            return;
        }

        double x;
        double y;
        double z;
        if (this.spawnAtExactLocation) {
            x = this.location.getX();
            y = this.location.getY();
            z = this.location.getZ();
        } else {
            double xRandomizer;
            double yRandomizer;
            double zRandomizer;
            // Random offset spans entire coordinate. Leave as is for block locations.
            // Real locations need the randomizer to include area around other blocks.
            if (isBlockLocation(this.location)) {
                xRandomizer = Math.random() * this.spread;
                yRandomizer = Math.random() * this.spread;
                zRandomizer = Math.random() * this.spread;
            } else {
                xRandomizer = (Math.random() - 0.5) * this.spread;
                yRandomizer = (Math.random() - 0.5) * this.spread;
                zRandomizer = (Math.random() - 0.5) * this.spread;
            }
            x = this.location.getX() + xRandomizer;
            y = this.location.getY() + yRandomizer;
            z = this.location.getZ() + zRandomizer;
        }

        if (this.forOnePlayerOnly) {
            assert this.player != null;
            this.player.spawnParticle(this.particleType, x, y, z, 0);
        } else {
            assert this.world != null;
            this.world.spawnParticle(this.particleType, x, y, z, 0);
        }
    }

    /**
     * Checks if this location is a block's location. Block locations are exact coordinates.
     * @param location Location to check
     * @return true if this location is an exact coordinate, false otherwise
     */
    public static boolean isBlockLocation(Location location) {
        boolean isXAnEdge = location.getX() == location.getBlockX();
        boolean isYAnEdge = location.getY() == location.getBlockY();
        boolean isZAnEdge = location.getZ() == location.getBlockZ();
        return isXAnEdge && isYAnEdge && isZAnEdge;
    }
}
