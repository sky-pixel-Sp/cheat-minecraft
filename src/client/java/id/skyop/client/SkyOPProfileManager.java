package id.skyop.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerInput;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Three JSON configuration slots, portable import/export, rotating backups and hotbar layouts.
 */
public final class SkyOPProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() { }.getType();
    private static final Path ROOT = net.fabricmc.loader.api.FabricLoader.getInstance()
            .getConfigDir().resolve("skyop-client");
    private static final Path PROFILE_DIR = ROOT.resolve("profiles");
    private static final Path BACKUP_DIR = ROOT.resolve("backups");
    private static final Path EXPORT_FILE = ROOT.resolve("skyop-profile-export.json");
    private static final Path IMPORT_FILE = ROOT.resolve("skyop-profile-import.json");

    private SkyOPProfileManager() { }

    public static Path exportPath() { return EXPORT_FILE; }
    public static Path importPath() { return IMPORT_FILE; }

    public static void saveSlot(int slot) throws IOException {
        checkSlot(slot);
        Files.createDirectories(PROFILE_DIR);
        backupCurrent();
        SkyOPCore.saveConfig();
        Properties p = loadCurrentProperties();
        Map<String, String> values = new HashMap<>();
        for (String name : p.stringPropertyNames()) values.put(name, p.getProperty(name));
        Files.writeString(slotPath(slot), GSON.toJson(values));
    }

    public static void loadSlot(int slot) throws IOException {
        checkSlot(slot);
        Map<String, String> values = readMap(slotPath(slot));
        apply(values);
    }

    public static void exportCurrent() throws IOException {
        Files.createDirectories(ROOT);
        SkyOPCore.saveConfig();
        Properties p = loadCurrentProperties();
        Map<String, String> values = new HashMap<>();
        for (String name : p.stringPropertyNames()) values.put(name, p.getProperty(name));
        Files.writeString(EXPORT_FILE, GSON.toJson(values));
    }

    public static void importProfile() throws IOException {
        Map<String, String> values = readMap(IMPORT_FILE);
        backupCurrent();
        apply(values);
    }

    public static void backupCurrent() throws IOException {
        if (!Files.exists(SkyOPCore.configPath())) {
            SkyOPCore.saveConfig();
        }
        Files.createDirectories(BACKUP_DIR);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Files.copy(SkyOPCore.configPath(), BACKUP_DIR.resolve("skyop-" + stamp + ".properties"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public static void saveHotbarPreset(int slot) throws IOException {
        checkSlot(slot);
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) throw new IOException("Join a world before saving a hotbar preset.");

        List<String> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(BuiltInRegistries.ITEM.getKey(client.player.getInventory().getItem(i).getItem()).toString());
        }
        Files.createDirectories(PROFILE_DIR);
        Files.writeString(hotbarPath(slot), GSON.toJson(items));
    }

    public static void loadHotbarPreset(int slot) throws IOException {
        checkSlot(slot);
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null
                || client.player.containerMenu != client.player.inventoryMenu) {
            throw new IOException("Open the player inventory context in a world to load a hotbar preset.");
        }

        List<String> desired = GSON.fromJson(Files.readString(hotbarPath(slot)),
                new TypeToken<List<String>>() { }.getType());
        if (desired == null || desired.size() != 9) throw new IOException("Invalid hotbar preset.");
        var inventory = client.player.getInventory();

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            String itemId = desired.get(hotbar);
            if (itemId == null || "minecraft:air".equals(itemId)) continue;
            String currentId = BuiltInRegistries.ITEM.getKey(inventory.getItem(hotbar).getItem()).toString();
            if (currentId.equals(itemId)) continue;

            int found = -1;
            // Do not disturb earlier hotbar positions that already match their preset.
            for (int source = hotbar + 1; source < 9; source++) {
                if (BuiltInRegistries.ITEM.getKey(inventory.getItem(source).getItem()).toString().equals(itemId)) {
                    found = source;
                    break;
                }
            }
            if (found < 0) {
                for (int source = 9; source < Math.min(36, inventory.getContainerSize()); source++) {
                    if (BuiltInRegistries.ITEM.getKey(inventory.getItem(source).getItem()).toString().equals(itemId)) {
                        found = source;
                        break;
                    }
                }
            }
            if (found >= 0) {
                int menuSlot = found < 9 ? 36 + found : found;
                client.gameMode.handleContainerInput(
                        client.player.inventoryMenu.containerId, menuSlot, hotbar, ContainerInput.SWAP, client.player
                );
            }
        }
    }

    private static void apply(Map<String, String> values) throws IOException {
        Properties p = new Properties();
        p.putAll(values);
        SkyOPConfigManager.save(SkyOPCore.configPath(), p);
        SkyOPCore.reloadConfigFromDisk();
    }

    private static Properties loadCurrentProperties() throws IOException {
        Properties p = new Properties();
        if (Files.exists(SkyOPCore.configPath())) {
            try (var reader = Files.newBufferedReader(SkyOPCore.configPath())) {
                p.load(reader);
            }
        }
        return p;
    }

    private static Map<String, String> readMap(Path path) throws IOException {
        if (!Files.exists(path)) throw new IOException("Profile not found: " + path);
        Map<String, String> values = GSON.fromJson(Files.readString(path), MAP_TYPE);
        if (values == null || values.isEmpty()) throw new IOException("Profile is empty or invalid.");
        return values;
    }

    private static Path slotPath(int slot) { return PROFILE_DIR.resolve("profile-" + slot + ".json"); }
    private static Path hotbarPath(int slot) { return PROFILE_DIR.resolve("hotbar-" + slot + ".json"); }

    private static void checkSlot(int slot) {
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("Profile slot must be 1, 2 or 3.");
    }
}
