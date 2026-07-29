package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.EffectSign;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.exception.InsufficientPermissionException;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/// On modify sign from template in {@code EffectSign}:
/// - apply colors
/// - register sign ability
@Component
public class PlayerSignChangeListener implements Listener {
    @Inject private RankService rankService;

    @EventHandler
    public void onPlayerSignChange(SignChangeEvent event) {
        EffectSign sign = EffectSign.getSignFromInput(event.getLine(1));
        if (sign == null) {
            return;
        }

        try {
            Player player = event.getPlayer();
            this.rankService.requireRank(MessagePrefix.GAME, Rank.STAFF, player);
            event.line(1, sign.getFormattedSignTextComponent());
        } catch (InsufficientPermissionException ignore) { }
    }
}
