package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TextUtil {

    public static String formatEnumName(Enum<?> enumVal) {
        return enumVal.name().toLowerCase().replace('_', ' ');
    }
}
