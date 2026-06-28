package com.mtxrii.contourmc.exception;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.Source;

public class SpawnpointArgumentException extends IllegalArgumentException {
    private final Message message;

    public SpawnpointArgumentException(String message) {
        final Message errMsg = new Message(MessagePrefix.SPAWN, true, message);
        super(errMsg.getMessage());
        this.message = errMsg;
    }

    public void sendTo(Source sender) {
        this.message.sendTo(sender);
    }

    public void sendTo(Player player) {
        this.message.sendTo(player);
    }
}
