package com.mtxrii.contourmc.exception;

import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;

public class InsufficientPermissionException extends CommandArgumentException {
    private static final String DEFAULT_MESSAGE = "You do not have permission to perform this action.";

    public InsufficientPermissionException(MessagePrefix prefix) {
        final Message errMsg = new Message(prefix, true, DEFAULT_MESSAGE);
        super(prefix, errMsg.getMessage());
    }
}
