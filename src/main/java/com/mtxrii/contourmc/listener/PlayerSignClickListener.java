package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.EffectSign;
import com.mtxrii.contourmc.exception.CommandArgumentException;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.KitService;
import com.mtxrii.contourmc.service.SpawnpointService;
import com.mtxrii.contourmc.util.GameUtil;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/// On player right-click sign block:
/// - If text matches kit sign:
///   - Equip kit
///   - Heal player
///   - Run test debug msg
///   - Respawn player
@Component
public class PlayerSignClickListener implements Listener {
    private Plugin plugin;
    private KitService kitService;
    private SpawnpointService spawnpointService;

    @Inject
    public PlayerSignClickListener(
            Plugin plugin,
            KitService kitService,
            SpawnpointService spawnpointService
    ) {
        this.plugin = plugin;
        this.kitService = kitService;
        this.spawnpointService = spawnpointService;
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
                GameUtil.healPlayer(player);
                new Message(
                        MessagePrefix.GAME,
                        "You've been healed"
                ).sendTo(player);
            }

            case KIT -> {
                String kitName = ((Sign) clickedBlock.getState()).getLine(2);
                if (kitName.isEmpty()) {
                    return;
                }
                try {
                    this.kitService.equipKit(kitName, player);
                } catch (CommandArgumentException e) {
                    new Message(
                            MessagePrefix.KIT,
                            true,
                            "This kit is currently unavailable"
                    ).sendTo(player);
                    return;
                }
                new Message(
                        MessagePrefix.KIT,
                        "Kit {} equipped successfully",
                        kitName
                ).sendTo(player);
            }

            case SPAWN -> {
                Set<String> spawnpointNames = this.spawnpointService.spawnpoints();
                if (spawnpointNames.isEmpty()) {
                    return;
                }
                int idx = ThreadLocalRandom.current().nextInt(spawnpointNames.size());
                Iterator<String> it = spawnpointNames.iterator();
                for (int i = 0; i < idx; i++) {
                    it.next();
                }
                String name = it.next();
                if (name == null) {
                    return;
                }
                this.spawnpointService.teleportLivingEntityToSpawnpoint(name, player);
            }
        }
    }
}
