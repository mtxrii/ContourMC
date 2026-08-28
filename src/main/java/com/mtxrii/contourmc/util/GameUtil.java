package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public final class GameUtil {
    private static final List<Material> SIGN_TYPES = List.of(
            Material.OAK_SIGN, Material.OAK_WALL_SIGN, Material.OAK_HANGING_SIGN, Material.OAK_WALL_HANGING_SIGN,
            Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN, Material.SPRUCE_HANGING_SIGN, Material.SPRUCE_WALL_HANGING_SIGN,
            Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN, Material.BIRCH_HANGING_SIGN, Material.BIRCH_WALL_HANGING_SIGN,
            Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN, Material.JUNGLE_HANGING_SIGN, Material.JUNGLE_WALL_HANGING_SIGN,
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN, Material.ACACIA_HANGING_SIGN, Material.ACACIA_WALL_HANGING_SIGN,
            Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN, Material.DARK_OAK_HANGING_SIGN, Material.DARK_OAK_WALL_HANGING_SIGN,
            Material.MANGROVE_SIGN, Material.MANGROVE_WALL_SIGN, Material.MANGROVE_HANGING_SIGN, Material.MANGROVE_WALL_HANGING_SIGN,
            Material.CHERRY_SIGN, Material.CHERRY_WALL_SIGN, Material.CHERRY_HANGING_SIGN, Material.CHERRY_WALL_HANGING_SIGN,
            Material.BAMBOO_SIGN, Material.BAMBOO_WALL_SIGN, Material.BAMBOO_HANGING_SIGN, Material.BAMBOO_WALL_HANGING_SIGN,
            Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN, Material.CRIMSON_HANGING_SIGN, Material.CRIMSON_WALL_HANGING_SIGN,
            Material.WARPED_SIGN, Material.WARPED_WALL_SIGN, Material.WARPED_HANGING_SIGN, Material.WARPED_WALL_HANGING_SIGN
    );
    private static final double PLAYER_MAX_HEALTH = 20.0;
    private static final int PLAYER_MAX_FOOD = 20;

    public static boolean isSign(Material material) {
        return SIGN_TYPES.contains(material);
    }

    public static void healPlayer(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        double playerMaxHealth = (maxHealthAttribute == null) ? PLAYER_MAX_HEALTH : maxHealthAttribute.getValue();
        player.setHealth(playerMaxHealth);
        player.setFoodLevel(PLAYER_MAX_FOOD);
    }

    public static Set<String> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers()
                     .stream()
                     .map(Player::getName)
                     .collect(Collectors.toSet());
    }

    public static boolean isOnGround(Player player, boolean strictMode) {
        Location loc = player.getLocation();
        boolean onGroundServerSide = loc.clone().subtract(0, 0.1, 0).getBlock().getType().isSolid();
        boolean onGroundClientSide = ((Entity) player).isOnGround(); // Hiding deprecated warning
        if (strictMode) {
            return onGroundServerSide && onGroundClientSide;
        } else {
            return onGroundServerSide || onGroundClientSide;
        }
    }
}
