package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.EffectSign;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.KitService;
import com.mtxrii.contourmc.util.GameUtil;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

/// On player right-click sign block:
/// - If text matches kit sign:
///   - Equip kit
@Component
public class PlayerSignClickListener implements Listener {
    private Plugin plugin;
    private KitService kitService;

    @Inject
    public PlayerSignClickListener(Plugin plugin, KitService kitService) {
        this.plugin = plugin;
        this.kitService = kitService;
    }

    @EventHandler
    public void onPlayerRightClickSign(PlayerInteractEvent event) {
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (clickedBlock == null || !GameUtil.isSign(clickedBlock.getType())) {
            return;
        }

        Player player = event.getPlayer();
        String signTxt = ((Sign) clickedBlock.getState()).getLine(1);
        EffectSign sign = EffectSign.getSignFromFormatted(signTxt);
        if (sign == null) {
            return;
        } else {
            event.setCancelled(true);
        }

        switch (sign) {
            case TEST -> {
                player.sendMessage("Test");
            }

            case HEAL -> {
                // @TODO: Move heal player method to GameUtil
                AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
                double playerMaxHealth = (maxHealthAttribute == null) ? 20.0 : maxHealthAttribute.getValue();
                player.setHealth(playerMaxHealth);
                player.setFoodLevel(20);
            }

            case KIT -> {
                String kitName = ((Sign) clickedBlock.getState()).getLine(2);
                if (kitName.isEmpty()) {
                    return;
                }
                this.kitService.equipKit(kitName, player); // @TODO: Surround with try-catch for CommandArgumentException
                new Message(
                        MessagePrefix.KIT,
                        "Kit {} equipped successfully",
                        kitName
                ).sendTo(player);
            }
        }
    }
}
