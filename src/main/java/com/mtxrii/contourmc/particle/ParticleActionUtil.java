package com.mtxrii.contourmc.particle;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

@UtilityClass
public final class ParticleActionUtil {

    /**
     * Damages the first valid living entity found near the given location and sends
     * a hit message to the player.
     *
     * @param senderHeadLocation the location to search around for nearby living entities
     * @param player the player causing the damage; this player is ignored if found in the search area
     * @param entityDamage the amount of damage to apply to the hit entity
     * @return {@code true} if an entity was damaged and the caller should stop further processing.
     *         {@code false} if no valid entity was found
     */
    public static boolean damageEntitiesInArea(Location senderHeadLocation, Player player, double entityDamage) {
        boolean hitPlayer = false;
        Collection<LivingEntity> entitiesAtLaser = senderHeadLocation.getNearbyLivingEntities(1);
        for (LivingEntity entity : entitiesAtLaser) {
            if (entity instanceof Player targetPlayer) {
                if (targetPlayer.getName().equals(player.getName())) {
                    continue;
                }
            }
            entity.damage(entityDamage, player);
            String entityName;
            if (entity instanceof Player targetPlayer) {
                entityName = targetPlayer.getName();
                hitPlayer = true;
            } else {
                entityName = entity.getName();
            }
            String entityHitName = hitPlayer ? "player" : "entity";
            new Message(
                    MessagePrefix.GAME,
                    "Hit {}: {}",
                    entityHitName,
                    entityName
            ).sendTo(player);
            return true;
        }
        return false;
    }
}
