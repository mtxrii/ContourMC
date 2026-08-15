package com.mtxrii.contourmc.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimeUtilTest {

    @Test
    void testInstantStringRoundtrip() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String str = TimeUtil.instantToString(now);
        Instant parsed = TimeUtil.stringToInstant(str);
        assertThat(parsed).isEqualTo(now);
    }

    @Test
    void testTimeUnitFromString() {
        assertThat(TimeUtil.TimeUnit.fromString("minute")).isEqualTo(TimeUtil.TimeUnit.MINUTES);
        assertThat(TimeUtil.TimeUnit.fromString("minutes")).isEqualTo(TimeUtil.TimeUnit.MINUTES);
        assertThat(TimeUtil.TimeUnit.fromString("DAYS")).isEqualTo(TimeUtil.TimeUnit.DAYS);
        assertThat(TimeUtil.TimeUnit.fromString("invalid")).isNull();
        assertThat(TimeUtil.TimeUnit.fromString(null)).isNull();
    }

    @Test
    void testGetInstantInTimeFromNow() {
        Instant futureMinutes = TimeUtil.getInstantInTimeFromNow(5, TimeUtil.TimeUnit.MINUTES);
        assertThat(futureMinutes).isAfter(Instant.now());

        Instant futureWeeks = TimeUtil.getInstantInTimeFromNow(1, TimeUtil.TimeUnit.WEEKS);
        assertThat(futureWeeks).isAfter(Instant.now());
    }
}
