package id.skyop.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/** Persistent client-side waypoint storage. */
public final class SkyOPWaypointManager {
    public record Waypoint(String name, int x, int y, int z) { }

    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("skyop-client").resolve("waypoints.properties");
    private static final List<Waypoint> WAYPOINTS = new ArrayList<>();
    private static final int MAX_WAYPOINTS = 32;

    private SkyOPWaypointManager() { }

    public static void load() {
        WAYPOINTS.clear();
        if (!Files.exists(FILE)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            properties.load(reader);
            int count;
            try {
                count = Math.max(0, Math.min(MAX_WAYPOINTS,
                        Integer.parseInt(properties.getProperty("count", "0"))));
            } catch (NumberFormatException ignored) {
                count = 0;
            }
            for (int i = 0; i < count; i++) {
                try {
                    int x = Integer.parseInt(properties.getProperty("point." + i + ".x"));
                    int y = Integer.parseInt(properties.getProperty("point." + i + ".y"));
                    int z = Integer.parseInt(properties.getProperty("point." + i + ".z"));
                    String name = properties.getProperty("point." + i + ".name", "Waypoint " + (i + 1));
                    WAYPOINTS.add(new Waypoint(safeName(name), x, y, z));
                } catch (RuntimeException ignored) {
                    // Skip malformed entries instead of preventing the client from loading.
                }
            }
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not load waypoints: " + exception.getMessage());
        }
    }

    public static boolean addCurrent(Minecraft client) {
        if (client == null || client.player == null || WAYPOINTS.size() >= MAX_WAYPOINTS) return false;
        int x = (int) Math.floor(client.player.getX());
        int y = (int) Math.floor(client.player.getY());
        int z = (int) Math.floor(client.player.getZ());
        WAYPOINTS.add(new Waypoint("Waypoint " + (WAYPOINTS.size() + 1), x, y, z));
        save();
        return true;
    }

    public static List<Waypoint> all() {
        return List.copyOf(WAYPOINTS);
    }

    public static List<Waypoint> nearest(Minecraft client, int limit) {
        if (client == null || client.player == null) return List.of();
        return WAYPOINTS.stream()
                .sorted(Comparator.comparingDouble(point -> distance(client, point)))
                .limit(Math.max(0, limit))
                .toList();
    }

    public static double distance(Minecraft client, Waypoint point) {
        if (client == null || client.player == null || point == null) return Double.POSITIVE_INFINITY;
        double dx = client.player.getX() - point.x();
        double dy = client.player.getY() - point.y();
        double dz = client.player.getZ() - point.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static void clear() {
        WAYPOINTS.clear();
        save();
    }

    private static String safeName(String value) {
        String name = value == null ? "Waypoint" : value.replaceAll("[\\p{Cntrl}]", "").strip();
        if (name.isEmpty()) name = "Waypoint";
        return name.length() > 32 ? name.substring(0, 32) : name;
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty("count", Integer.toString(WAYPOINTS.size()));
        for (int i = 0; i < WAYPOINTS.size(); i++) {
            Waypoint point = WAYPOINTS.get(i);
            properties.setProperty("point." + i + ".name", point.name());
            properties.setProperty("point." + i + ".x", Integer.toString(point.x()));
            properties.setProperty("point." + i + ".y", Integer.toString(point.y()));
            properties.setProperty("point." + i + ".z", Integer.toString(point.z()));
        }
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                properties.store(writer, "SkyOP Client waypoints");
            }
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not save waypoints: " + exception.getMessage());
        }
    }
}
