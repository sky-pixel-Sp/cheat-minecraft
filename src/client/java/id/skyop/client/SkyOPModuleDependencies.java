package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

/**
 * Declares runtime context requirements and safe auto-disable rules for modules.
 */
public final class SkyOPModuleDependencies {
    public enum Requirement { CLIENT, WORLD, PLAYER, INVENTORY, FISHING_ROD, BLOCK_TARGET }

    private SkyOPModuleDependencies() { }

    public static Requirement requirement(SkyOPClient.Module module) {
        return switch (module) {
            case AUTO_ARMOR, AUTO_TOOL, AUTO_REFILL, AUTO_EAT, INVENTORY_CLEANER, HOTBAR_PRESETS ->
                    Requirement.INVENTORY;
            case AUTO_FISH -> Requirement.FISHING_ROD;
            case AUTO_JUMP, STEP_ASSIST, SPIDER, SPEED, FLIGHT, NO_FALL, KILL_AURA,
                    TRIGGER_BOT, AUTO_CLICKER, AUTO_HUNT, AUTO_BRIDGE, INFINITE_JUMP, JUMP_BOOST,
                    ENTITY_ESP, ITEM_ESP, WAYPOINTS, MINIMAP, GAMMA_BOOST, NIGHT_VISION -> Requirement.WORLD;
            default -> Requirement.CLIENT;
        };
    }

    public static boolean isAvailable(Minecraft client, SkyOPClient.Module module) {
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

    public static String missingRequirement(Minecraft client, SkyOPClient.Module module) {
        if (isAvailable(client, module)) return "";
        return switch (requirement(module)) {
            case CLIENT -> "";
            case WORLD -> "Join a world first.";
            case PLAYER -> "A player entity is required.";
            case INVENTORY -> "Open the normal player inventory context first.";
            case FISHING_ROD -> "Hold a fishing rod in your main hand.";
            case BLOCK_TARGET -> "Aim at a block first.";
        };
    }
}
