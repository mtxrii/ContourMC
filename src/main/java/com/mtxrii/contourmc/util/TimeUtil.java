package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public final class TimeUtil {
    public enum TimeUnit {
        MINUTES(ChronoUnit.MINUTES),
        HOURS(ChronoUnit.HOURS),
        DAYS(ChronoUnit.DAYS),
        WEEKS(ChronoUnit.WEEKS),
        MONTHS(ChronoUnit.MONTHS),
        YEARS(ChronoUnit.YEARS),
        DECADES(ChronoUnit.DECADES);

        private final ChronoUnit chronoUnit;

        TimeUnit(ChronoUnit chronoUnit) {
            this.chronoUnit = chronoUnit;
        }

        public static TimeUnit fromString(String str) {
            if (str == null) {
                return null;
            }

            if (!str.toUpperCase().endsWith("S")) {
                str += "S";
            }

            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static Set<String> getNormalizedNames() {
            return Arrays.stream(values())
                         .map(timeUnit -> StringUtils.capitalize(timeUnit.name().toLowerCase()))
                         .collect(Collectors.toSet());
        }
    }

    public static String instantToString(Instant instant) {
        return instant.toString();
    }

    public static Instant stringToInstant(String instant) {
        return Instant.parse(instant);
    }

    public static Instant getInstantInTimeFromNow(long timeToAdd, TimeUnit timeUnit) {
        return switch (timeUnit) {
            case WEEKS -> Instant.now().plus(Duration.ofDays(timeToAdd * 7));
            case MONTHS -> Instant.now().plus(Duration.ofHours(Math.round(timeToAdd * 30.5 * 24)));
            case YEARS -> Instant.now().plus(Duration.ofDays(timeToAdd * 365));
            case DECADES -> Instant.now().plus(Duration.ofDays(timeToAdd * 365 * 10));
            default -> Instant.now().plus(timeToAdd, timeUnit.chronoUnit);
        };
    }
}
