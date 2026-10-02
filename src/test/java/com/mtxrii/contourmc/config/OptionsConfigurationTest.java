package com.mtxrii.contourmc.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionsConfigurationTest {

    @Test
    void createsAnEmptyOptionsDocument() {
        OptionsConfiguration configuration = new OptionsConfiguration();

        assertThat(configuration.options).isEmpty();
    }

    @Test
    void storesStringKeyValuePairs() {
        OptionsConfiguration configuration = new OptionsConfiguration();

        configuration.options.put("spawn-world", "world");
        configuration.options.put("motd", "Welcome");

        assertThat(configuration.options)
                .containsEntry("spawn-world", "world")
                .containsEntry("motd", "Welcome");
    }

    @Test
    void storesServerJoinableAsAStringValueInTheOptionsMap() {
        OptionsConfiguration configuration = new OptionsConfiguration();

        configuration.options.put(OptionsConfiguration.SERVER_JOINABLE, Boolean.toString(false));

        assertThat(configuration.options)
                .containsEntry(OptionsConfiguration.SERVER_JOINABLE, "false");
    }
}
