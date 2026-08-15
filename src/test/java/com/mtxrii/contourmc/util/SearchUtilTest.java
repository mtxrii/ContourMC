package com.mtxrii.contourmc.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SearchUtilTest {

    @Test
    void testFindClosestPlayerNameMatch() {
        String[] candidates = {"Notch", "Dinnerbone", "Mtxrii", "Jeb_", "Grumm"};

        // Exact match
        assertThat(SearchUtil.findClosestPlayerNameMatch("Notch", candidates)).isEqualTo("Notch");

        // Case-insensitive match
        assertThat(SearchUtil.findClosestPlayerNameMatch("notch", candidates)).isEqualTo("Notch");

        // Prefix match
        assertThat(SearchUtil.findClosestPlayerNameMatch("Mtx", candidates)).isEqualTo("Mtxrii");

        // Contains match
        assertThat(SearchUtil.findClosestPlayerNameMatch("nerbone", candidates)).isEqualTo("Dinnerbone");

        // Fuzzy match
        assertThat(SearchUtil.findClosestPlayerNameMatch("Nothc", candidates)).isEqualTo("Notch");

        // No match
        assertThat(SearchUtil.findClosestPlayerNameMatch("UnknownPlayerXYZ", candidates)).isNull();
        assertThat(SearchUtil.findClosestPlayerNameMatch(null, candidates)).isNull();
        assertThat(SearchUtil.findClosestPlayerNameMatch("Notch", null)).isNull();
        assertThat(SearchUtil.findClosestPlayerNameMatch("", candidates)).isNull();
    }

    @Test
    void testFindClosestStringStartingMatch() {
        Set<String> candidates = Set.of("apple", "apricot", "banana", "blueberry");

        // Exact match
        assertThat(SearchUtil.findClosestStringStartingMatch("apple", candidates)).isEqualTo("apple");

        // Case-insensitive exact match
        assertThat(SearchUtil.findClosestStringStartingMatch("BANANA", candidates)).isEqualTo("banana");

        // Prefix match
        assertThat(SearchUtil.findClosestStringStartingMatch("ap", candidates)).isIn("apple", "apricot");

        // No match starting with
        assertThat(SearchUtil.findClosestStringStartingMatch("cherry", candidates)).isNull();
        assertThat(SearchUtil.findClosestStringStartingMatch(null, candidates)).isNull();
        assertThat(SearchUtil.findClosestStringStartingMatch("ap", null)).isNull();
    }
}
