package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;

@UtilityClass
public final class TimeUtil {

    public static String instantToString(Instant instant) {
        return instant.toString();
    }

    public static Instant stringToInstant(String instant) {
        return Instant.parse(instant);
    }
}
