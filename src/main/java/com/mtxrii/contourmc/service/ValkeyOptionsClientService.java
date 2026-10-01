package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.config.OptionsConfiguration;
import com.sxtanna.platform.archetype.Component;
import io.valkey.Jedis;
import io.valkey.JedisPool;
import io.valkey.JedisPoolConfig;
import io.valkey.Transaction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Persists {@link OptionsConfiguration} as a Valkey hash.
 *
 * <p>The configured key is the hash key and every option is stored as one hash
 * field. Saving replaces the complete document atomically, so removed options
 * are not left behind in Valkey.</p>
 *
 * <p>This class owns the connection pool and must be closed when the plugin is
 * disabled.</p>
 */
@Component
@Singleton
public final class ValkeyOptionsClientService implements AutoCloseable {
    public static final String COMBAT_LOGGING_ENABLED_OPTION = "COMBAT_LOGGING_ENABLED";

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private static final int DEFAULT_TIMEOUT = 2_000;
    private static final String DEFAULT_OPTIONS_KEY = "contourmc:options";

    private final JedisPool pool;
    private final String optionsKey;

    @Getter
    private volatile boolean combatLoggingEnabled;

    /**
     * Creates the application-managed Valkey service.
     *
     * <p>Settings are read from Java system properties first and environment
     * variables second. Defaults target a local Valkey server.</p>
     *
     * <ul>
     *     <li>{@code contourmc.valkey.host} / {@code CONTOURMC_VALKEY_HOST}</li>
     *     <li>{@code contourmc.valkey.port} / {@code CONTOURMC_VALKEY_PORT}</li>
     *     <li>{@code contourmc.valkey.timeout} / {@code CONTOURMC_VALKEY_TIMEOUT}</li>
     *     <li>{@code contourmc.valkey.password} / {@code CONTOURMC_VALKEY_PASSWORD}</li>
     *     <li>{@code contourmc.valkey.options-key} / {@code CONTOURMC_VALKEY_OPTIONS_KEY}</li>
     * </ul>
     */
    @Inject
    public ValkeyOptionsClientService(Plugin plugin) {
        this(
                setting("contourmc.valkey.host", "CONTOURMC_VALKEY_HOST", DEFAULT_HOST),
                integerSetting("contourmc.valkey.port", "CONTOURMC_VALKEY_PORT", DEFAULT_PORT),
                integerSetting("contourmc.valkey.timeout", "CONTOURMC_VALKEY_TIMEOUT", DEFAULT_TIMEOUT),
                optionalSetting("contourmc.valkey.password", "CONTOURMC_VALKEY_PASSWORD"),
                setting("contourmc.valkey.options-key", "CONTOURMC_VALKEY_OPTIONS_KEY", DEFAULT_OPTIONS_KEY)
        );
    }

    /**
     * Creates a client for a standalone Valkey server.
     *
     * @param host Valkey hostname
     * @param port Valkey port
     * @param timeout connection and socket timeout in milliseconds
     * @param password password, or {@code null} when authentication is disabled
     * @param optionsKey Valkey hash key used for the options document
     */
    public ValkeyOptionsClientService(
            @NotNull String host,
            int port,
            int timeout,
            @Nullable String password,
            @NotNull String optionsKey
    ) {
        this.optionsKey = requireText(optionsKey, "optionsKey");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.pool = new JedisPool(
                poolConfig,
                requireText(host, "host"),
                port,
                timeout,
                password
        );

        String configuredCombatLogging = this.get(COMBAT_LOGGING_ENABLED_OPTION);
        if (configuredCombatLogging == null) {
            this.combatLoggingEnabled = true; // @TODO: Store defaults somewhere
            this.set(COMBAT_LOGGING_ENABLED_OPTION, Boolean.toString(this.combatLoggingEnabled));
        } else {
            this.combatLoggingEnabled = Boolean.parseBoolean(configuredCombatLogging);
        }
    }

    /**
     * Loads the complete options document. A missing Valkey hash is returned as
     * an empty configuration.
     */
    @NotNull
    public OptionsConfiguration load() {
        try (Jedis jedis = this.pool.getResource()) {
            OptionsConfiguration configuration = new OptionsConfiguration();
            configuration.options.putAll(jedis.hgetAll(this.optionsKey));
            return configuration;
        }
    }

    /**
     * Replaces the complete options document in one Valkey transaction.
     */
    public void save(@NotNull OptionsConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        Map<String, String> options = new LinkedHashMap<>(configuration.options);
        if (options.keySet().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Option keys cannot be null");
        }
        if (options.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Option values cannot be null");
        }

        try (Jedis jedis = this.pool.getResource()) {
            Transaction transaction = jedis.multi();
            transaction.del(this.optionsKey);
            if (!options.isEmpty()) {
                transaction.hset(this.optionsKey, options);
            }
            transaction.exec();
        }
    }

    /** Sets or replaces one option. */
    public void set(@NotNull String option, @NotNull String value) {
        String validatedOption = requireText(option, "option");
        String validatedValue = Objects.requireNonNull(value, "value");
        try (Jedis jedis = this.pool.getResource()) {
            jedis.hset(this.optionsKey, validatedOption, validatedValue);
        }
        if (COMBAT_LOGGING_ENABLED_OPTION.equals(validatedOption)) {
            this.combatLoggingEnabled = Boolean.parseBoolean(validatedValue);
        }
    }

    /** Returns one option, or {@code null} when it does not exist. */
    @Nullable
    public String get(@NotNull String option) {
        try (Jedis jedis = this.pool.getResource()) {
            return jedis.hget(this.optionsKey, requireText(option, "option"));
        }
    }

    /** Removes one option, returning whether an option was removed. */
    public boolean remove(@NotNull String option) {
        try (Jedis jedis = this.pool.getResource()) {
            return jedis.hdel(this.optionsKey, requireText(option, "option")) > 0;
        }
    }

    @Override
    public void close() {
        this.pool.close();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be null or blank");
        }
        return value;
    }

    private static String setting(String property, String environmentVariable, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String optionalSetting(String property, String environmentVariable) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null || value.isBlank() ? null : value;
    }

    private static int integerSetting(String property, String environmentVariable, int defaultValue) {
        String value = setting(property, environmentVariable, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer value for " + property + ": " + value, exception);
        }
    }
}
