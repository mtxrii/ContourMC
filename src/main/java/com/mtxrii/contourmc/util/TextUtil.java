package com.mtxrii.contourmc.util;

import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public final class TextUtil {
    static final DateTimeFormatter INSTANT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    public static String formatEnumName(Enum<?> enumVal) {
        if (enumVal.getClass().equals(Rank.class)) {
            return StringUtils.capitalize(enumVal.name().toLowerCase());
        }
        return enumVal.name().toLowerCase().replace('_', ' ');
    }

    public static String formatLocation(Location location) {
        final String coords = '(' +
                        location.getBlockX() +
                        ", " +
                        location.getBlockY() +
                        ", " +
                        location.getBlockZ()
                        + ')';
        final String worldName = location.getWorld().getName();
        return coords + " " + worldName;
    }

    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return "null";
        }
        return INSTANT_FORMATTER.format(instant);
    }

    public static Message getNoPlayerFoundMessage(MessagePrefix messagePrefix, String playerName) {
        return new Message(
                messagePrefix,
                true,
                "No player found with name {}",
                playerName
        );
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isBlank();
    }
}
