package com.mtxrii.contourmc.message;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MessageConst {

    public static final Message CLEAR_ITEM_NAME_COMMAND_COMPLETED_MSG = new Message(
            MessagePrefix.GAME,
            "Main hand item name formatting cleared."
    );

    public static final Message FEATURE_IS_UNAVAILABLE = new Message(
            MessagePrefix.GAME,
            true,
            "Feature is momentarily unavailable. Please try again later."
    );

    public static final Message NO_SPAWNS_STORED = new Message(
            MessagePrefix.SPAWN,
            true,
            "No spawns available to teleport to."
    );

    public static final Message SANCTION_MSG_BORDER = new Message(
            MessagePrefix.BLANK,
            "\n&c&l██████████████████████████████&b\n"
    );
}
