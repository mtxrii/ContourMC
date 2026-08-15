package com.mtxrii.contourmc.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextUtilTest {

    @Test
    void testIsEmpty() {
        assertThat(TextUtil.isEmpty(null)).isTrue();
        assertThat(TextUtil.isEmpty("")).isTrue();
        assertThat(TextUtil.isEmpty("   ")).isTrue();
        assertThat(TextUtil.isEmpty("hello")).isFalse();
    }

    @Test
    void testFormatInstantNull() {
        assertThat(TextUtil.formatInstant(null)).isEqualTo("null");
    }
}
