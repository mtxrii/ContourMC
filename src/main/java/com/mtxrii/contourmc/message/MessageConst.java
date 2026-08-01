package com.mtxrii.contourmc.message;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MessageConst {

    public static final Message FEATURE_IS_UNAVAILABLE = new Message(
            MessagePrefix.GAME,
            true,
            "Feature is momentarily unavailable. Please try again later."
    );
}
