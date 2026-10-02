package com.mtxrii.contourmc.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options stored as a single NoSQL document.
 *
 * <p>The configuration deliberately contains only the document data. A NoSQL
 * repository or adapter can persist {@link #options} without requiring this
 * model to know which NoSQL implementation is being used.</p>
 */
@ConfigSerializable
public class OptionsConfiguration {
    public static final String SERVER_JOINABLE = "SERVER_JOINABLE";

    @Setting
    public Map<String, String> options = new LinkedHashMap<>();
}
