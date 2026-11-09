package id.skyop.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPConfigManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void migratesLegacyConfigAndBacksUpBeforeWriting() throws Exception {
        Path config = temporaryDirectory.resolve("config/skyop-client.properties");
        Path backups = temporaryDirectory.resolve("config/skyop-client/backups");
        Files.createDirectories(config.getParent());
        String original = "module.HUD=true\nlanguage=id\nauraRange=5.0\n";
        Files.writeString(config, original);

        Properties migrated = SkyOPConfigManager.load(config, backups);

        assertEquals("2", migrated.getProperty("configVersion"));
        assertEquals("en", migrated.getProperty("language"));
        assertEquals("true", migrated.getProperty("module.HUD"));
        assertEquals("5.0", migrated.getProperty("auraRange"));
        assertEquals(1, countFiles(backups));
        assertEquals(original, Files.readString(firstFile(backups)));
        assertEquals("2", readConfig(config).getProperty("configVersion"));
    }

    @Test
    void missingConfigReturnsEnglishVersionedDefaultsWithoutCreatingAFile() throws Exception {
        Path config = temporaryDirectory.resolve("config/skyop-client.properties");
        Properties loaded = SkyOPConfigManager.load(config, temporaryDirectory.resolve("backups"));

        assertEquals("2", loaded.getProperty("configVersion"));
        assertEquals("en", loaded.getProperty("language"));
        assertFalse(Files.exists(config));
    }

    @Test
    void malformedPropertiesAreBackedUpAndRecovered() throws Exception {
        Path config = temporaryDirectory.resolve("config/skyop-client.properties");
        Path backups = temporaryDirectory.resolve("backups");
        Files.createDirectories(config.getParent());
        String malformed = "broken=\\uZZZZ";
        Files.writeString(config, malformed);

        Properties loaded = SkyOPConfigManager.load(config, backups);

        assertEquals("2", loaded.getProperty("configVersion"));
        assertEquals("en", loaded.getProperty("language"));
        assertEquals(1, countFiles(backups));
        assertEquals(malformed, Files.readString(firstFile(backups)));
        assertEquals("2", readConfig(config).getProperty("configVersion"));
    }

    @Test
    void refusesToOverwriteAConfigFromANewerVersion() throws Exception {
        Path config = temporaryDirectory.resolve("config/skyop-client.properties");
        Files.createDirectories(config.getParent());
        String content = "configVersion=99" + System.lineSeparator() + "language=en" + System.lineSeparator();
        Files.writeString(config, content);

        assertThrows(IOException.class,
                () -> SkyOPConfigManager.load(config, temporaryDirectory.resolve("backups")));
        assertEquals(content, Files.readString(config));
    }

    @Test
    void saveWritesVersionedEnglishConfigAndRoundTripsValues() throws Exception {
        Path config = temporaryDirectory.resolve("config/skyop-client.properties");
        Properties values = new Properties();
        values.setProperty("language", "id");
        values.setProperty("auraRange", "4.5");

        SkyOPConfigManager.save(config, values);

        Properties loaded = readConfig(config);
        assertEquals("2", loaded.getProperty("configVersion"));
        assertEquals("en", loaded.getProperty("language"));
        assertEquals("4.5", loaded.getProperty("auraRange"));
        try (Stream<Path> files = Files.list(config.getParent())) {
            assertEquals(1, files.count(), "temporary files must be removed after atomic save");
        }
    }

    private static Properties readConfig(Path path) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        return properties;
    }

    private static long countFiles(Path directory) throws IOException {
        if (!Files.exists(directory)) return 0;
        try (Stream<Path> files = Files.list(directory)) {
            return files.count();
        }
    }

    private static Path firstFile(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.findFirst().orElseThrow();
        }
    }
}
