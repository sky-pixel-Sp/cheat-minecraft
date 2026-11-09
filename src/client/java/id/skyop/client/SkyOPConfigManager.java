package id.skyop.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Properties;

/**
 * Owns SkyOP configuration persistence, schema versioning, migration backups,
 * corruption recovery, and atomic file replacement.
 *
 * All paths are supplied by the caller and must stay inside the mod config directory.
 */
public final class SkyOPConfigManager {
    public static final int CURRENT_CONFIG_VERSION = 2;

    private SkyOPConfigManager() { }

    public static Properties load(Path configFile, Path backupDirectory) throws IOException {
        if (!Files.exists(configFile)) {
            Properties defaults = new Properties();
            defaults.setProperty("configVersion", Integer.toString(CURRENT_CONFIG_VERSION));
            defaults.setProperty("language", "id");
            return defaults;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(configFile)) {
            properties.load(reader);
        } catch (IllegalArgumentException malformed) {
            backup(configFile, backupDirectory, "corrupt");
            Properties defaults = new Properties();
            defaults.setProperty("configVersion", Integer.toString(CURRENT_CONFIG_VERSION));
            defaults.setProperty("language", "id");
            save(configFile, defaults);
            return defaults;
        }

        int version;
        try {
            version = Integer.parseInt(properties.getProperty("configVersion", "0"));
        } catch (NumberFormatException invalidVersion) {
            backup(configFile, backupDirectory, "invalid-version");
            Properties defaults = new Properties();
            defaults.setProperty("configVersion", Integer.toString(CURRENT_CONFIG_VERSION));
            defaults.setProperty("language", "id");
            save(configFile, defaults);
            return defaults;
        }

        if (version < 0) {
            backup(configFile, backupDirectory, "invalid-version");
            Properties defaults = new Properties();
            defaults.setProperty("configVersion", Integer.toString(CURRENT_CONFIG_VERSION));
            defaults.setProperty("language", "id");
            save(configFile, defaults);
            return defaults;
        }
        if (version > CURRENT_CONFIG_VERSION) {
            throw new IOException("SkyOP config was created by a newer version ("
                    + version + "); refusing to overwrite it.");
        }

        if (version < CURRENT_CONFIG_VERSION || !("id".equals(properties.getProperty("language", "id")) || "en".equals(properties.getProperty("language", "id")))) {
            backup(configFile, backupDirectory, "migration-v" + version);
            migrate(properties, version);
            save(configFile, properties);
        } else {
            properties.setProperty("configVersion", Integer.toString(CURRENT_CONFIG_VERSION));
        }
        return properties;
    }

    public static void save(Path configFile, Properties values) throws IOException {
        Path parent = configFile.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("Configuration path has no parent directory.");
        Files.createDirectories(parent);

        Properties snapshot = new Properties();
        snapshot.putAll(values);
        snapshot.setProperty("configVersion", Integer.toString(CURRENT_CONFIG_VERSION));
        String language = snapshot.getProperty("language", "id");
        snapshot.setProperty("language", "id".equals(language) || "en".equals(language) ? language : "id");

        Path temporary = Files.createTempFile(parent, "skyop-config-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                snapshot.store(writer, "SkyOP Client configuration");
            }
            try {
                Files.move(temporary, configFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static void migrate(Properties properties, int startingVersion) throws IOException {
        int version = startingVersion;
        while (version < CURRENT_CONFIG_VERSION) {
            switch (version) {
                case 0 -> migrateV0ToV1(properties);
                case 1 -> migrateV1ToV2(properties);
                default -> throw new IOException("No SkyOP configuration migration from version " + version);
            }
            version++;
            properties.setProperty("configVersion", Integer.toString(version));
        }
        String language = properties.getProperty("language", "id");
        properties.setProperty("language", "id".equals(language) || "en".equals(language) ? language : "id");
    }

    private static void migrateV0ToV1(Properties properties) {
        // Version 1 adds schema tracking without changing existing preferences.
        properties.putIfAbsent("language", "id");
    }

    private static void migrateV1ToV2(Properties properties) {
        // Version 2 preserves both supported in-client languages.
        String language = properties.getProperty("language", "id");
        properties.setProperty("language", "id".equals(language) || "en".equals(language) ? language : "id");
    }

    private static void backup(Path source, Path backupDirectory, String reason) throws IOException {
        if (!Files.exists(source)) return;
        Files.createDirectories(backupDirectory);
        String timestamp = Instant.now().toString().replace(':', '-');
        Path backup = Files.createTempFile(backupDirectory,
                "skyop-" + reason + "-" + timestamp + "-", ".properties");
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
    }
}
