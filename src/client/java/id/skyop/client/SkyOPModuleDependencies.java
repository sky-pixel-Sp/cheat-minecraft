package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

/**
 * Declares runtime context requirements and safe auto-disable rules for modules.
 */
public final class SkyOPModuleDependencies {
    public enum Requirement { CLIENT, WORLD, PLAYER, INVENTORY, FISHING_ROD, BLOCK_TARGET }

    private SkyOPModuleDependencies() { }

    public static Requirement requirement(SkyOPModuleId module) {
        return switch (module) {
            case AUTO_ARMOR, AUTO_TOTEM, AUTO_TOOL, AUTO_REFILL, AUTO_EAT, INVENTORY_CLEANER, HOTBAR_PRESETS ->
                    Requirement.INVENTORY;
            case AUTO_FISH -> Requirement.FISHING_ROD;
            case AUTO_JUMP, STEP_ASSIST, SPIDER, SPEED, FLIGHT, NO_FALL, KILL_AURA,
                    TRIGGER_BOT, AUTO_CLICKER, AUTO_HUNT, AUTO_BRIDGE, AUTO_MLG, INFINITE_JUMP, JUMP_BOOST,
                    ENTITY_ESP, ITEM_ESP, WAYPOINTS, MINIMAP, GAMMA_BOOST, NIGHT_VISION,
                    FAST_BREAK, AUTO_WALK, ZOOM, CONTAINER_FINDER, AUTO_RESPAWN,
                    LOW_HEALTH_ALERT, ARMOR_ALERT -> Requirement.WORLD;
            default -> Requirement.CLIENT;
        };
    }

    public static boolean isAvailable(Minecraft client, SkyOPModuleId module) {
        Requirement requirement = requirement(module);
        if (requirement == Requirement.CLIENT) return true;
        if (client == null || client.player == null || client.level == null) return false;
        if (requirement == Requirement.INVENTORY) {
            return client.player.containerMenu == client.player.inventoryMenu && !client.player.isSpectator();
        }
        if (requirement == Requirement.FISHING_ROD) {
            return client.player.getMainHandItem().is(Items.FISHING_ROD);
        }
        return true;
    }

    public static String missingRequirement(Minecraft client, SkyOPModuleId module) {
        if (isAvailable(client, module)) return "";
        return switch (requirement(module)) {
            case CLIENT -> "";
            case WORLD -> SkyOPLanguage.text("dependency.join_world");
            case PLAYER -> SkyOPLanguage.text("dependency.player_required");
            case INVENTORY -> SkyOPLanguage.text("dependency.inventory_required");
            case FISHING_ROD -> SkyOPLanguage.text("dependency.fishing_rod_required");
            case BLOCK_TARGET -> SkyOPLanguage.text("dependency.block_target_required");
        };
    }
}
