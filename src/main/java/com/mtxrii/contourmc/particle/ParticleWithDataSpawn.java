package com.mtxrii.contourmc.particle;

import com.mtxrii.contourmc.message.MessageConst;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a particle spawn with specialized or required data. Includes some presets
 * @source <a href="https://github.com/BloomMC/BloomMC-core/blob/main/src/main/java/com/mtxrii/bloom/core/ParticleWithDataSpawn.java">BloomMC-core ParticleSpawn.java</a>
 */
@Slf4j
public abstract class ParticleWithDataSpawn<T> extends ParticleSpawn {
    public enum ParticleColor {
        // Default Colors
        WHITE(Color.WHITE),
        SILVER(Color.SILVER),
        GRAY(Color.GRAY),
        BLACK(Color.BLACK),
        RED(Color.RED),
        MAROON(Color.MAROON),
        YELLOW(Color.YELLOW),
        OLIVE(Color.OLIVE),
        LIME(Color.LIME),
        GREEN(Color.GREEN),
        AQUA(Color.AQUA),
        TEAL(Color.TEAL),
        BLUE(Color.BLUE),
        NAVY(Color.NAVY),
        FUCHSIA(Color.FUCHSIA),
        PURPLE(Color.PURPLE),
        ORANGE(Color.ORANGE),

        // Custom Colors - Dyes
        BROWN(Color.fromRGB(153, 102, 51)),
        CYAN(Color.fromRGB(51, 204, 204)),
        LIGHT_BLUE(Color.fromRGB(102, 255, 255)),
        LIGHT_GRAY(Color.fromRGB(208, 208, 225)),
        MAGENTA(Color.fromRGB(255, 51, 204)),
        PINK(Color.fromRGB(255, 153, 255)),

        // Custom Colors - Other
        BLOOD_RED(Color.fromRGB(0x990E0E)),

        RANDOM(null);

        private final Color bukkitColor;

        ParticleColor(Color color) {
            this.bukkitColor = color;
        }

        public Color getBukkitColor() {
            if (this.equals(RANDOM)) {
                int randomInt = (int) (Math.random() * (ParticleColor.values().length));
                return ParticleColor.values()[randomInt].getBukkitColor();
            } else {
                return bukkitColor;
            }
        }

        public static ParticleColor fromBukkitColor(Color color) {
            if (color == null) {
                return null;
            }

            for (ParticleColor pc : ParticleColor.values()) {
                if (color.equals(pc.bukkitColor)) {
                    return pc;
                }
            }
            return null;
        }
    }

    public ParticleWithDataSpawn(Particle particleType, Player forPlayer, Location location, int count, double spread) {
        super(particleType, forPlayer, location, count, spread);
    }

    public ParticleWithDataSpawn(Particle particleType, Player forPlayer, Location location) {
        super(particleType, forPlayer, location);
    }

    public ParticleWithDataSpawn(Particle particleType, World forAllPlayersInWorld, Location location, int count, double spread) {
        super(particleType, forAllPlayersInWorld, location, count, spread);
    }

    public ParticleWithDataSpawn(Particle particleType, World forAllPlayersInWorld, Location location) {
        super(particleType, forAllPlayersInWorld, location);
    }

    public static class EntityEffectParticleSpawn extends ParticleWithDataSpawn<Color> {
        private static final Particle particleType = Particle.ENTITY_EFFECT;
        private final ParticleColor color;

        public EntityEffectParticleSpawn(Player forPlayer, Location location, int count, double spread, ParticleColor color) {
            super(particleType, forPlayer, location, count, spread);
            this.color = color;
        }

        public EntityEffectParticleSpawn(Player forPlayer, Location location, ParticleColor color) {
            super(particleType, forPlayer, location);
            this.color = color;
        }

        public EntityEffectParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, ParticleColor color) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.color = color;
        }

        public EntityEffectParticleSpawn(World forAllPlayersInWorld, Location location, ParticleColor color) {
            super(particleType, forAllPlayersInWorld, location);
            this.color = color;
        }

        @Override
        protected Color generateData() {
            return this.color.getBukkitColor();
        }
    }

    public static class DustParticleSpawn extends ParticleWithDataSpawn<Particle.DustOptions> {
        private static final Particle particleType = Particle.DUST;
        private final ParticleColor color;
        private final float size;

        public DustParticleSpawn(Player forPlayer, Location location, int count, double spread, ParticleColor color, float size) {
            super(particleType, forPlayer, location, count, spread);
            this.color = color;
            this.size = size;
        }

        public DustParticleSpawn(Player forPlayer, Location location, ParticleColor color, float size) {
            super(particleType, forPlayer, location);
            this.color = color;
            this.size = size;
        }

        public DustParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, ParticleColor color, float size) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.color = color;
            this.size = size;
        }

        public DustParticleSpawn(World forAllPlayersInWorld, Location location, ParticleColor color, float size) {
            super(particleType, forAllPlayersInWorld, location);
            this.color = color;
            this.size = size;
        }

        @Override
        protected Particle.DustOptions generateData() {
            return new Particle.DustOptions(this.color.getBukkitColor(), this.size);
        }
    }

    public static class DustColorTransitionParticleSpawn extends ParticleWithDataSpawn<Particle.DustTransition> {
        private static final Particle particleType = Particle.DUST_COLOR_TRANSITION;
        private final ParticleColor colorFrom;
        private final ParticleColor colorTo;
        private final float size;

        public DustColorTransitionParticleSpawn(Player forPlayer, Location location, int count, double spread, ParticleColor colorFrom, ParticleColor colorTo, float size) {
            super(particleType, forPlayer, location, count, spread);
            this.colorFrom = colorFrom;
            this.colorTo = colorTo;
            this.size = size;
        }

        public DustColorTransitionParticleSpawn(Player forPlayer, Location location, ParticleColor colorFrom, ParticleColor colorTo, float size) {
            super(particleType, forPlayer, location);
            this.colorFrom = colorFrom;
            this.colorTo = colorTo;
            this.size = size;
        }

        public DustColorTransitionParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, ParticleColor colorFrom, ParticleColor colorTo, float size) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.colorFrom = colorFrom;
            this.colorTo = colorTo;
            this.size = size;
        }

        public DustColorTransitionParticleSpawn(World forAllPlayersInWorld, Location location, ParticleColor colorFrom, ParticleColor colorTo, float size) {
            super(particleType, forAllPlayersInWorld, location);
            this.colorFrom = colorFrom;
            this.colorTo = colorTo;
            this.size = size;
        }

        @Override
        protected Particle.DustTransition generateData() {
            return new Particle.DustTransition(this.colorFrom.getBukkitColor(), this.colorTo.getBukkitColor(), this.size);
        }
    }

    public static class ItemParticleSpawn extends ParticleWithDataSpawn<ItemStack> {
        private static final Particle particleType = Particle.ITEM;
        private final ItemStack itemStack;

        public ItemParticleSpawn(Player forPlayer, Location location, int count, double spread, ItemStack itemStack) {
            super(particleType, forPlayer, location, count, spread);
            this.itemStack = itemStack;
        }

        public ItemParticleSpawn(Player forPlayer, Location location, ItemStack itemStack) {
            super(particleType, forPlayer, location);
            this.itemStack = itemStack;
        }

        public ItemParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, ItemStack itemStack) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.itemStack = itemStack;
        }

        public ItemParticleSpawn(World forAllPlayersInWorld, Location location, ItemStack itemStack) {
            super(particleType, forAllPlayersInWorld, location);
            this.itemStack = itemStack;
        }

        @Override
        protected ItemStack generateData() {
            return this.itemStack;
        }
    }

    public static class BlockRelatedParticleSpawn extends ParticleWithDataSpawn<BlockData> {
        public enum BlockRelatedParticle {BLOCK, FALLING_DUST, DUST_PILLAR, BLOCK_MARKER}

        private final BlockData blockParticleData;

        public BlockRelatedParticleSpawn(Player forPlayer, Location location, int count, double spread, BlockRelatedParticle particleType, BlockData particleBlockData) {
            super(getParticleType(particleType), forPlayer, location, count, spread);
            this.blockParticleData = particleBlockData;
        }

        public BlockRelatedParticleSpawn(Player forPlayer, Location location, BlockRelatedParticle particleType, BlockData particleBlockData) {
            super(getParticleType(particleType), forPlayer, location);
            this.blockParticleData = particleBlockData;
        }

        public BlockRelatedParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, BlockRelatedParticle particleType, BlockData particleBlockData) {
            super(getParticleType(particleType), forAllPlayersInWorld, location, count, spread);
            this.blockParticleData = particleBlockData;
        }

        public BlockRelatedParticleSpawn(World forAllPlayersInWorld, Location location, BlockRelatedParticle particleType, BlockData particleBlockData) {
            super(getParticleType(particleType), forAllPlayersInWorld, location);
            this.blockParticleData = particleBlockData;
        }

        @Override
        protected BlockData generateData() {
            return this.blockParticleData;
        }

        private static Particle getParticleType(BlockRelatedParticle blockRelatedParticleType) {
            return switch (blockRelatedParticleType) {
                case BLOCK -> Particle.BLOCK;
                case FALLING_DUST -> Particle.FALLING_DUST;
                case DUST_PILLAR -> Particle.DUST;
                case BLOCK_MARKER -> Particle.BLOCK_MARKER;
            };
        }
    }

    public static class VibrationParticleSpawn extends ParticleWithDataSpawn<Vibration> {
        private static final Particle particleType = Particle.VIBRATION;
        private final int arrivalTime;


        public VibrationParticleSpawn(Player forPlayer, Location location, int count, double spread, int arrivalTime) {
            super(particleType, forPlayer, location, count, spread);
            this.arrivalTime = arrivalTime;
        }

        public VibrationParticleSpawn(Player forPlayer, Location location, int arrivalTime) {
            super(particleType, forPlayer, location);
            this.arrivalTime = arrivalTime;
        }

        public VibrationParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, int arrivalTime) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.arrivalTime = arrivalTime;
        }

        public VibrationParticleSpawn(World forAllPlayersInWorld, Location location, int arrivalTime) {
            super(particleType, forAllPlayersInWorld, location);
            this.arrivalTime = arrivalTime;
        }

        @Override
        protected Vibration generateData() {
            Vibration.Destination vibrationDestination = new Vibration.Destination.BlockDestination(super.location);
            return new Vibration(vibrationDestination, this.arrivalTime);
        }
    }

    public static class SculkChargeParticleSpawn extends ParticleWithDataSpawn<Float> {
        private static final Particle particleType = Particle.SCULK_CHARGE;
        private final float power;

        public SculkChargeParticleSpawn(Player forPlayer, Location location, int count, double spread, float power) {
            super(particleType, forPlayer, location, count, spread);
            this.power = power;
        }

        public SculkChargeParticleSpawn(Player forPlayer, Location location, float power) {
            super(particleType, forPlayer, location);
            this.power = power;
        }

        public SculkChargeParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, float power) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.power = power;
        }

        public SculkChargeParticleSpawn(World forAllPlayersInWorld, Location location, float power) {
            super(particleType, forAllPlayersInWorld, location);
            this.power = power;
        }

        @Override
        protected Float generateData() {
            return Float.valueOf(this.power);
        }
    }

    public static class ShriekParticleSpawn extends ParticleWithDataSpawn<Integer> {
        private static final Particle particleType = Particle.SHRIEK;
        private final int power;

        public ShriekParticleSpawn(Player forPlayer, Location location, int count, double spread, int power) {
            super(particleType, forPlayer, location, count, spread);
            this.power = power;
        }

        public ShriekParticleSpawn(Player forPlayer, Location location, int power) {
            super(particleType, forPlayer, location);
            this.power = power;
        }

        public ShriekParticleSpawn(World forAllPlayersInWorld, Location location, int count, double spread, int power) {
            super(particleType, forAllPlayersInWorld, location, count, spread);
            this.power = power;
        }

        public ShriekParticleSpawn(World forAllPlayersInWorld, Location location, int power) {
            super(particleType, forAllPlayersInWorld, location);
            this.power = power;
        }

        @Override
        protected Integer generateData() {
            return Integer.valueOf(this.power);
        }
    }

    protected abstract T generateData();

    @Override
    protected void spawnParticle() {
        T data = this.generateData();

        if (super.particleType.getDataType() != data.getClass()) {
            log.warn(
                    "Cannot spawn particle of type '{}' as it requires data of type '{}'",
                    super.particleType.name(),
                    data.getClass().getName()
            );
            if (this.forOnePlayerOnly) {
                assert this.player != null;
                MessageConst.FEATURE_IS_UNAVAILABLE.sendTo(this.player);
            }
            return;
        }

        double x;
        double y;
        double z;
        if (super.spawnAtExactLocation) {
            x = super.location.getX();
            y = super.location.getY();
            z = super.location.getZ();
        } else {
            double xRandomizer;
            double yRandomizer;
            double zRandomizer;
            // Random offset spans entire coordinate. Leave as is for block locations.
            // Real locations need the randomizer to include area around other blocks.
            if (isBlockLocation(super.location)) {
                xRandomizer = Math.random() * super.spread;
                yRandomizer = Math.random() * super.spread;
                zRandomizer = Math.random() * super.spread;
            } else {
                xRandomizer = (Math.random() - 0.5) * super.spread;
                yRandomizer = (Math.random() - 0.5) * super.spread;
                zRandomizer = (Math.random() - 0.5) * super.spread;
            }
            x = super.location.getX() + xRandomizer;
            y = super.location.getY() + yRandomizer;
            z = super.location.getZ() + zRandomizer;
        }

        if (super.forOnePlayerOnly) {
            assert super.player != null;
            super.player.spawnParticle(super.particleType, x, y, z, 0, data);
        } else {
            assert super.world != null;
            super.world.spawnParticle(super.particleType, x, y, z, 0, data);
        }
    }
}
