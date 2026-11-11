package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Central runtime state. Gameplay behaviour is owned by concrete module classes;
 * this class only stores values that must be shared or persisted.
 */
public final class SkyOPState {
    private SkyOPState() { }

    public record GlowState(Entity entity, boolean wasGlowing) { }
    public record HudPosition(int x, int y) { }

    static final EnumSet<SkyOPModuleId> ENABLED = EnumSet.of(SkyOPModuleId.HUD);
    static final EnumSet<SkyOPModuleId> FAVORITES = EnumSet.noneOf(SkyOPModuleId.class);
    static final Map<SkyOPModuleId, KeyMapping> MODULE_KEYBINDS = new EnumMap<>(SkyOPModuleId.class);
    static final Map<SkyOPModuleId, Integer> MODULE_KEY_CODES = new EnumMap<>(SkyOPModuleId.class);
    static final Map<SkyOPModuleId, HudPosition> HUD_POSITIONS = new EnumMap<>(SkyOPModuleId.class);
    static final Map<UUID, GlowState> ORIGINAL_GLOW = new HashMap<>();
    static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skyop-client.properties");

    public static boolean targetPlayers = false;
    public static boolean targetMobs = true;
    public static boolean auraRotate = true;
    public static boolean auraRequireLineOfSight = true;
    public static boolean auraTargetLowestHealth = false;
    public static double auraFov = 110.0;
    public static double auraRotationSpeed = 0.42;
    public static double auraRange = 4.2;
    public static int auraDelayTicks = 8;
    public static int triggerDelayTicks = 3;
    public static int autoClickerCps = 12;
    public static double speedMultiplier = 1.7;
    public static double flightSpeed = 0.38;
    public static int autoJumpDelayTicks = 4;
    public static double spiderClimbSpeed = 0.22;
    public static double espRange = 48.0;
    public static boolean espPlayers = true;
    public static boolean espMobs = true;
    public static boolean espItems = true;
    public static double autoHuntRange = 4.5;
    public static double jumpBoostMultiplier = 1.35;
    public static double fastBreakLevel = 7.0;
    public static int bridgeDelayTicks = 2;
    public static int autoEatThreshold = 14;
    public static int autoRefillThreshold = 2;
    public static int hudX = 8;
    public static int hudY = 8;
    public static int hudAccentColor = 0xFF61C7F2;
    public static String customHudText = "SkyOP Client • Have a great game!";

    static final int[] HUD_PALETTE = {
            0xFF61C7F2, 0xFF8B7CFF, 0xFFFFB45B, 0xFF57D69A, 0xFFFF7885, 0xFFF1F6FB
    };

    static int cpsWindowTicks;
    static int attacksThisWindow;
    static int currentCps;
    static Vec3 previousPosition;
    static double currentSpeedBps;
}
