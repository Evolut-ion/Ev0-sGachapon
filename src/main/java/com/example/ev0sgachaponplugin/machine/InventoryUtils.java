package com.example.ev0sgachaponplugin.machine;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.lang.reflect.Method;
import java.util.Locale;

@SuppressWarnings("removal")
public final class InventoryUtils {

    private InventoryUtils() {
    }

    public static int countItem(Player player, String itemId) {
        Inventory inventory = getInventory(player);
        if (inventory == null) {
            return 0;
        }
        int count = 0;
        for (ItemContainer container : getContainers(inventory)) {
            if (container == null) {
                continue;
            }
            short capacity = container.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                ItemStack stack = container.getItemStack(slot);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                if (itemId.equals(resolveStackKey(stack))) {
                    count += stack.getQuantity();
                }
            }
        }
        return count;
    }

    public static boolean removeItem(Player player, String itemId, int quantity) {
        Inventory inventory = getInventory(player);
        if (inventory == null) {
            return false;
        }
        if (quantity <= 0) {
            return true;
        }
        if (countItem(player, itemId) < quantity) {
            return false;
        }

        int remaining = quantity;
        for (ItemContainer container : getContainers(inventory)) {
            if (container == null) {
                continue;
            }
            short capacity = container.getCapacity();
            for (short slot = 0; slot < capacity && remaining > 0; slot++) {
                ItemStack stack = container.getItemStack(slot);
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                if (!itemId.equals(resolveStackKey(stack))) {
                    continue;
                }

                int stackQuantity = stack.getQuantity();
                int toRemove = Math.min(stackQuantity, remaining);
                if (stackQuantity > toRemove) {
                    container.setItemStackForSlot(slot, new ItemStack(itemId, stackQuantity - toRemove));
                } else {
                    container.setItemStackForSlot(slot, null);
                }
                remaining -= toRemove;
            }
        }

        markInventoryChanged(inventory);
        return true;
    }

    public static void giveItem(Player player, String itemId, int quantity) {
        Inventory inventory = getInventory(player);
        if (inventory == null) {
            return;
        }
        if (quantity <= 0) {
            return;
        }

        ItemContainer destination = inventory.getCombinedHotbarFirst();
        if (destination != null) {
            destination.addItemStack(new ItemStack(itemId, quantity));
        }
        markInventoryChanged(inventory);
    }

    public static boolean hasAccessibleInventory(Player player) {
        return getInventory(player) != null;
    }

    public static void markInventoryChanged(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        try {
            Class<?> inventoryClass = inventory.getClass();
            for (Method method : inventoryClass.getMethods()) {
                String name = method.getName().toLowerCase(Locale.ROOT);
                if (method.getParameterCount() == 0 && (name.contains("mark") || name.contains("notify") || name.contains("changed"))) {
                    try {
                        method.invoke(inventory);
                        return;
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static ItemContainer[] getContainers(Inventory inventory) {
        return new ItemContainer[]{inventory.getHotbar(), inventory.getStorage()};
    }

    private static Inventory getInventory(Player player) {
        if (player == null) {
            return null;
        }
        try {
            return player.getInventory();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String resolveStackKey(ItemStack stack) {
        try {
            String itemId = stack.getItemId();
            if (!itemId.isBlank()) {
                return itemId;
            }
        } catch (Throwable ignored) {
        }
        try {
            String blockKey = stack.getBlockKey();
            if (blockKey != null && !blockKey.isBlank()) {
                return blockKey;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }
}