package com.mtxrii.contourmc.command.meta;

import com.mtxrii.contourmc.command.annotation.RequireRank;
import org.incendo.cloud.key.CloudKey;

public final class CommandMetaKeys {

    public static final CloudKey<RequireRank> REQUIRED_RANK = CloudKey.of(
            "contourmc:required_rank",
            RequireRank.class
    );

    private CommandMetaKeys() { }
}