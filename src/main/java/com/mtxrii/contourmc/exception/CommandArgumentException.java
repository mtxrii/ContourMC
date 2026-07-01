package com.mtxrii.contourmc.exception;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.Source;

public class CommandArgumentException extends IllegalArgumentException {
    private final Message message;

    public CommandArgumentException(MessagePrefix prefix, String message) {
        final Message errMsg = new Message(prefix, true, message);
        super(errMsg.getMessage());
        this.message = errMsg;
    }

    public void sendTo(Source sender) {
        this.message.sendTo(sender);
    }

    public void sendTo(Player player) {
        this.message.sendTo(player);
    }

    public Component getMessageComponent() {
        return this.message.getMessageComponent();
    }
}
