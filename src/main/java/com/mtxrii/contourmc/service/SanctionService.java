package com.mtxrii.contourmc.service;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessageConst;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

@Component
public class SanctionService {

    public void kick(Player target, String reason) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                
                &b&lYou've been kicked!
                
                {}
                
                
                
                """,
                reason
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICK_COMMAND);
    }

    public void kick(Player target) {
        Message kickMessage = MessageConst.SANCTION_MSG_BORDER.append(new Message(
                MessagePrefix.BLANK,
                """
                
                
                &b&lYou've been kicked!
                
                
                
                """
        )).append(MessageConst.SANCTION_MSG_BORDER);

        target.kick(kickMessage.getMessageComponent(), PlayerKickEvent.Cause.KICK_COMMAND);
    }
}
