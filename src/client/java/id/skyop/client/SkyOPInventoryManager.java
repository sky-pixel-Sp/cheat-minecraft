package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

/**
 * Survival inventory utilities. All item movement is submitted as ordinary vanilla
 * container input; the server remains responsible for validating inventory state.
 */
public final class SkyOPInventoryManager {
    private static final Set<EquipmentSlot> ARMOR_SLOTS = Set.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    );
    private static final Set<Object> UNSAFE_FOOD = Set.of(
            Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO,
            Items.PUFFERFISH, Items.CHORUS_FRUIT, Items.SUSPICIOUS_STEW, Items.CHICKEN
    );

    private static int eatRetry;
    private static int eatDelay;
    private static boolean eating;
    private static boolean ownsUseKey;
    private static int originalEatSlot = -1;
    private static int activeEatSlot = -1;
    private static int armorDelay;
    private static int refillDelay;
    private static int cleanDelay;
    private static int fishDelay;
    private static int autoToolSlot = -1;
    private static int previousToolSlot = -1;

    private SkyOPInventoryManager() { }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            resetWithoutPlayer();
            return;
        }

        if (SkyOPClient.isEnabled(SkyOPClient.Module.AUTO_EAT)) tickAutoEat(client);
        else stopEating(client);

        if (client.gui.screen() != null || client.player.isSpectator()
                || client.player.containerMenu != client.player.inventoryMenu) return;

        if (SkyOPClient.isEnabled(SkyOPClient.Module.AUTO_ARMOR)) tickAutoArmor(client);
        else armorDelay = 0;

        if (SkyOPClient.isEnabled(SkyOPClient.Module.AUTO_TOOL)) tickAutoTool(client);
        else restoreAutoTool(client);

        if (SkyOPClient.isEnabled(SkyOPClient.Module.AUTO_REFILL)) tickAutoRefill(client);
        else refillDelay = 0;

        if (SkyOPClient.isEnabled(SkyOPClient.Module.INVENTORY_CLEANER)) tickInventoryCleaner(client);
        else cleanDelay = 0;

        if (SkyOPClient.isEnabled(SkyOPClient.Module.AUTO_FISH)) tickAutoFish(client);
        else fishDelay = 0;
    }

    public static void stop(Minecraft client) {
        stopEating(client);
        restoreAutoTool(client);
        armorDelay = 0;
        refillDelay = 0;
        cleanDelay = 0;
        fishDelay = 0;
    }

    private static void tickAutoEat(Minecraft client) {
        var player = client.player;
        if (eatRetry > 0) eatRetry--;
        if (eatDelay > 0) {
            eatDelay--;
            if (eating && player.isUsingItem()) holdUse(client);
            return;
        }

        int foodLevel = player.getFoodData().getFoodLevel();
        if (!eating) {
            if (foodLevel >= SkyOPClient.autoEatThreshold || player.isUsingItem()
                    || (client.options.keyUse.isDown() && !ownsUseKey) || eatRetry > 0) return;

            int slot = findFoodHotbar(player.getInventory(), 20 - foodLevel);
            if (slot < 0) {
                eatRetry = 20;
                return;
            }

            originalEatSlot = player.getInventory().getSelectedSlot();
            activeEatSlot = slot;
            player.getInventory().setSelectedSlot(slot);
            eating = true;
            eatDelay = 1;
            return;
        }

        if (player.getInventory().getSelectedSlot() != activeEatSlot) {
            stopEating(client);
            return;
        }

        ItemStack food = player.getMainHandItem();
        if (foodLevel >= 20 || !isSafeFood(food)) {
            stopEating(client);
            return;
        }

        if (player.isUsingItem()) {
            holdUse(client);
            return;
        }

        client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        if (player.isUsingItem()) holdUse(client);
        else eatDelay = 2;
    }

    private static int findFoodHotbar(net.minecraft.world.entity.player.Inventory inventory, int missing) {
        int bestFit = -1;
        int bestFitNutrition = -1;
        int weakestSlot = -1;
        int weakestNutrition = Integer.MAX_VALUE;

        for (int slot = 0; slot < net.minecraft.world.entity.player.Inventory.SELECTION_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!isSafeFood(stack)) continue;
            int nutrition = stack.get(DataComponents.FOOD).nutrition();
            if (nutrition <= missing && nutrition > bestFitNutrition) {
                bestFit = slot;
                bestFitNutrition = nutrition;
            }
            if (nutrition < weakestNutrition) {
                weakestNutrition = nutrition;
                weakestSlot = slot;
            }
        }
        return bestFit >= 0 ? bestFit : weakestSlot;
    }

    private static boolean isSafeFood(ItemStack stack) {
        if (stack == null || stack.isEmpty() || UNSAFE_FOOD.contains(stack.getItem())) return false;
        var food = stack.get(DataComponents.FOOD);
        return food != null && food.nutrition() > 0;
    }

    private static void holdUse(Minecraft client) {
        client.options.keyUse.setDown(true);
        ownsUseKey = true;
    }

    private static void releaseUse(Minecraft client) {
        if (!ownsUseKey) return;
        ownsUseKey = false;
        client.options.keyUse.setDown(false);
    }

    private static void stopEating(Minecraft client) {
        if (client.player != null) {
            releaseUse(client);
            if (eating && client.player.isUsingItem() && client.gameMode != null) {
                client.gameMode.releaseUsingItem(client.player);
            }
            if (eating && originalEatSlot >= 0
                    && client.player.getInventory().getSelectedSlot() == activeEatSlot) {
                client.player.getInventory().setSelectedSlot(originalEatSlot);
            }
        } else {
            ownsUseKey = false;
        }

        eating = false;
        originalEatSlot = -1;
        activeEatSlot = -1;
        eatDelay = 0;
    }

    private static void tickAutoArmor(Minecraft client) {
        if (armorDelay > 0) { armorDelay--; return; }
        var player = client.player;
        var inventory = player.getInventory();

        for (int slot = 9; slot < Math.min(36, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || !ARMOR_SLOTS.contains(equippable.slot())) continue;
            if (!player.getItemBySlot(equippable.slot()).isEmpty()) continue;

            client.gameMode.handleContainerInput(
                    player.inventoryMenu.containerId, slot, 0, ContainerInput.QUICK_MOVE, player
            );
            armorDelay = 8;
            return;
        }
    }

    private static void tickAutoTool(Minecraft client) {
        if (!client.options.keyAttack.isDown()
                || !(client.hitResult instanceof BlockHitResult blockHit)) {
            restoreAutoTool(client);
            return;
        }

        BlockState state = client.level.getBlockState(blockHit.getBlockPos());
        var inventory = client.player.getInventory();
        int selected = inventory.getSelectedSlot();
        float bestSpeed = inventory.getItem(selected).getDestroySpeed(state);
        int bestSlot = selected;

        for (int slot = 0; slot < net.minecraft.world.entity.player.Inventory.SELECTION_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        if (bestSlot != selected) {
            if (autoToolSlot < 0) previousToolSlot = selected;
            inventory.setSelectedSlot(bestSlot);
            autoToolSlot = bestSlot;
        }
    }

    private static void restoreAutoTool(Minecraft client) {
        if (client.player != null && autoToolSlot >= 0 && previousToolSlot >= 0
                && client.player.getInventory().getSelectedSlot() == autoToolSlot) {
            client.player.getInventory().setSelectedSlot(previousToolSlot);
        }
        autoToolSlot = -1;
        previousToolSlot = -1;
    }

    private static void tickAutoRefill(Minecraft client) {
        if (refillDelay > 0) { refillDelay--; return; }
        var player = client.player;
        var inventory = player.getInventory();
        int selected = inventory.getSelectedSlot();
        ItemStack current = inventory.getItem(selected);
        if (current.isEmpty() || current.getCount() > SkyOPClient.autoRefillThreshold) return;

        for (int slot = 9; slot < Math.min(36, inventory.getContainerSize()); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !ItemStack.isSameItemSameComponents(candidate, current)) continue;
            client.gameMode.handleContainerInput(
                    player.inventoryMenu.containerId, slot, selected, ContainerInput.SWAP, player
            );
            refillDelay = 12;
            return;
        }
    }

    /**
     * Consolidates matching stacks into already-existing hotbar stacks. It never drops items.
     */
    private static void tickInventoryCleaner(Minecraft client) {
        if (cleanDelay > 0) { cleanDelay--; return; }
        var player = client.player;
        var inventory = player.getInventory();

        for (int slot = 9; slot < Math.min(36, inventory.getContainerSize()); slot++) {
            ItemStack source = inventory.getItem(slot);
            if (source.isEmpty() || source.getCount() >= source.getMaxStackSize()) continue;

            boolean hasRoom = false;
            for (int hotbar = 0; hotbar < net.minecraft.world.entity.player.Inventory.SELECTION_SIZE; hotbar++) {
                ItemStack target = inventory.getItem(hotbar);
                if (!target.isEmpty() && ItemStack.isSameItemSameComponents(source, target)
                        && target.getCount() < target.getMaxStackSize()) {
                    hasRoom = true;
                    break;
                }
            }

            if (hasRoom) {
                client.gameMode.handleContainerInput(
                        player.inventoryMenu.containerId, slot, 0, ContainerInput.QUICK_MOVE, player
                );
                cleanDelay = 30;
                return;
            }
        }
    }

    /**
     * Retrieves a bite only when the 26.3 fishing-hook accessor reports it. Then recasts after
     * a short delay. It uses the ordinary fishing-rod action and server-side catch calculation.
     */
    private static void tickAutoFish(Minecraft client) {
        if (fishDelay > 0) { fishDelay--; return; }
        var player = client.player;
        ItemStack rod = player.getMainHandItem();
        if (!rod.is(Items.FISHING_ROD)) return;

        var hook = player.fishing;
        if (hook == null || !hook.isAlive()) {
            client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            fishDelay = 20;
            return;
        }

        if (((SkyOPFishingHookAccessor) hook).skyop$isBiting()) {
            hook.retrieve(rod);
            fishDelay = 24;
        }
    }

    private static void resetWithoutPlayer() {
        ownsUseKey = false;
        eating = false;
        originalEatSlot = -1;
        activeEatSlot = -1;
        eatDelay = 0;
        eatRetry = 0;
        armorDelay = 0;
        refillDelay = 0;
        cleanDelay = 0;
        fishDelay = 0;
        autoToolSlot = -1;
        previousToolSlot = -1;
    }
}
