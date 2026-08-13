package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Resolves IANA timezones from IP addresses using MaxMind's local GeoIP2 City database.
 *
 * <p>The database is deliberately local: player IP addresses are never sent to an external
 * service. Install a current {@code GeoLite2-City.mmdb} at
 * {@code plugins/ContourMC/GeoLite2-City.mmdb} to enable lookups.</p>
 */
@Component
@Singleton
public final class IpTimezoneLookupService implements AutoCloseable {
    private static final String DATABASE_FILE_NAME = "GeoLite2-City.mmdb";

    private final DatabaseReader databaseReader;

    @Inject
    public IpTimezoneLookupService(@NotNull Plugin plugin) {
        Path databasePath = plugin.getDataPath().resolve(DATABASE_FILE_NAME);
        this.databaseReader = this.openDatabase(databasePath, plugin);
    }

    /**
     * Returns the timezone recorded for {@code address}, or an empty value if the database is
     * unavailable or has no timezone for that address.
     */
    public Optional<ZoneId> lookup(@NotNull InetAddress address) {
        if (this.databaseReader == null) {
            return Optional.empty();
        }

        try {
            String timezone = this.databaseReader.city(address).getLocation().getTimeZone();
            if (timezone == null || timezone.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(ZoneId.of(timezone));
        } catch (IOException | GeoIp2Exception | DateTimeException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.databaseReader != null) {
            this.databaseReader.close();
        }
    }

    private DatabaseReader openDatabase(Path databasePath, Plugin plugin) {
        if (!Files.isRegularFile(databasePath)) {
            plugin.getLogger().warning(
                    "IP timezone lookup is disabled. Download GeoLite2-City.mmdb and place it at " + databasePath
            );
            return null;
        }

        try {
            return new DatabaseReader.Builder(databasePath.toFile())
                    .withCache(new CHMCache())
                    .build();
        } catch (IOException exception) {
            plugin.getLogger().warning(
                    "Unable to open the GeoIP database at " + databasePath + ": " + exception.getMessage()
            );
            return null;
        }
    }
}
