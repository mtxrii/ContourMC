package com.mtxrii.contourmc.exception;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import net.kyori.adventure.text.Component;

public class CommandArgumentException extends IllegalArgumentException {
    private final Message message;

    public CommandArgumentException(MessagePrefix prefix, String message) {
        final Message errMsg = new Message(prefix, true, message);
        super(errMsg.getMessage());
        this.message = errMsg;
    }

    public Component getMessageComponent() {
        return this.message.getMessageComponent();
    }
}
