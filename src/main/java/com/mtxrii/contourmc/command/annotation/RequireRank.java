package com.mtxrii.contourmc.command.annotation;

import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.MessagePrefix;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public @interface RequireRank {
    Rank value();

    MessagePrefix prefix() default MessagePrefix.ENV;

    boolean allowConsole() default true;
}
