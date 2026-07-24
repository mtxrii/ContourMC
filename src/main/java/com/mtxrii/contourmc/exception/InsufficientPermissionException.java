package com.mtxrii.contourmc.exception;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import net.kyori.adventure.text.Component;

public class InsufficientPermissionException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "You do not have permission to perform this action.";

    private final Message message;

    public InsufficientPermissionException(MessagePrefix prefix) {
        final Message errMsg = new Message(prefix, true, DEFAULT_MESSAGE);
        super(errMsg.getMessage());
        this.message = errMsg;
    }

    public Component getMessageComponent() {
        return this.message.getMessageComponent();
    }
}
