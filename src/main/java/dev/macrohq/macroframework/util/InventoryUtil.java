package dev.macrohq.macroframework.util;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import org.jetbrains.annotations.Nullable;

public class InventoryUtil {

    /**
     * Click an item in the currently open inventory / gui.
     *
     * @param slot   Slot number of the item you want to click.
     * @param button <br>
     *               - 0: Left click. <br>
     *               - 1: Right click. <br>
     *               - 2: Middle click, not sure.
     * @param type   <br>
     *               - 0: PICKUP - Regular click <br>
     *               - 1: QUICK_MOVE - Shift click to move from inventory to container for example. <br>
     *               - 2: SWAP - Not sure, would not recommend using. <br>
     *               - 3: CLONE - Not sure, would not recommend using. <br>
     *               - 4: Throw - Throw away an item from an inventory. <br>
     *               - 5: QUICK_CRAFT - Quick craft, again I don't see this being useful so don't use. <br>
     *               - 6: PICKUP_ALL - Don't know if it's different to PICKUP.
     * @return Whether the slot was successfully clicked. To prevent ping-less clicks,
     * it won't click unless there's an item in the slot.
     */
    public static boolean clickSlot(int slot, int button, int type) {
        Container openContainer = Ref.player().openContainer;
        if (openContainer == null) return false;
        Slot slotObj = openContainer.getSlot(slot);
        if (slotObj == null || !slotObj.getHasStack()) return false;
        Ref.mc().playerController.windowClick(Ref.player().openContainer.windowId, slot, button, type, Ref.player());
        return true;
    }

    /**
     * Check if an item which display name contains the given name is in the player's hotbar.
     *
     * @param name Part of the name you want to find without control codes.
     * @return Whether there is an item which name contains given string.
     */
    public static boolean isInHotbar(String name) {
        return getInHotbar(name) != null;
    }

    /**
     * Check if an item which display name is the given name is in the player's hotbar.
     *
     * @param name The name you want to find without control codes.
     * @return Whether there is an item which name is the given string.
     */
    public static boolean isInHotbarExact(String name) {
        return getInHotbarExact(name) != null;
    }

    /**
     * Get an ItemStack which display name contains the given name.
     *
     * @param name Part of the name you want to find without color codes.
     * @return The first ItemStack that contains given name or null if none fit.
     */
    public static @Nullable ItemStack getInHotbar(String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if (StringUtils.stripControlCodes(stack.getDisplayName()).contains(name))
                return stack;
        }
        return null;
    }

    /**
     * Get an ItemStack which display name is exactly the given name.
     *
     * @param name The name you want to find without color codes.
     * @return The first ItemStack that has the given name or null if none fit.
     */
    public static @Nullable ItemStack getInHotbarExact(String name) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if (StringUtils.stripControlCodes(stack.getDisplayName()).equals(name))
                return stack;
        }
        return null;
    }

    /**
     * Get the slot number of an item which display name contains the given name.
     * @param name Part of the name you want to find without color codes.
     * @return The first slot number that contains given name or -1 if none fit.
     */
    public static int getSlotInHotbar(String name) {
        for(int i = 0; i < 9; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if(StringUtils.stripControlCodes(stack.getDisplayName()).contains(name))
                return i;
        }
        return -1;
    }

    /**
     * Get the slot number of an item which display name is exactly the given name.
     * @param name The name you want to find without color codes.
     * @return The first slot number that has the given name or -1 if none fit.
     */
    public static int getSlotInHotbarExact(String name) {
        for(int i = 0; i < 9; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if(StringUtils.stripControlCodes(stack.getDisplayName()).equals(name))
                return i;
        }
        return -1;
    }

    /**
     * Check if an item which display name contains the given name is in the currently open container / gui.
     *
     * @param name Part of the name you want to find without control codes.
     * @return Whether there is an item which name contains given string.
     */
    public static boolean isInOpenContainer(String name) {
        return getInOpenContainer(name) != null;
    }

    /**
     * Check if an item which display name is the given name is in the currently open container / gui.
     *
     * @param name The name you want to find without control codes.
     * @return Whether there is an item which name is the given string.
     */
    public static boolean isInOpenContainerExact(String name) {
        return getInOpenContainerExact(name) != null;
    }

    /**
     * Get an ItemStack which display name contains the given name in the currently open container / gui.
     *
     * @param name Part of the name you want to find without color codes.
     * @return The first ItemStack that contains given name or null if none fit.
     */
    public static @Nullable ItemStack getInOpenContainer(String name) {
        Container openContainer = Ref.player().openContainer;
        if(openContainer == null) return null;
        for(int i = 0; i < openContainer.inventorySlots.size(); i++) {
            Slot slot = openContainer.getSlot(i);
            if(slot == null) continue;
            ItemStack stack = slot.getStack();
            if(stack == null) continue;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).contains(name))
                return stack;
        }
        return null;
    }

    /**
     * Get an ItemStack which display name is exactly the given name in the currently open container / gui.
     *
     * @param name The name you want to find without color codes.
     * @return The first ItemStack that has the given name or null if none fit.
     */
    public static @Nullable ItemStack getInOpenContainerExact(String name) {
        Container openContainer = Ref.player().openContainer;
        if(openContainer == null) return null;
        for(int i = 0; i < openContainer.inventorySlots.size(); i++) {
            Slot slot = openContainer.getSlot(i);
            if(slot == null) continue;
            ItemStack stack = slot.getStack();
            if(stack == null) continue;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).equals(name))
                return stack;
        }
        return null;
    }

    /**
     * Get the slot number of an item which display name contains the given name in the currently open container / gui.
     *
     * @param name Part of the name you want to find without color codes.
     * @return The first slot number that contains given name or -1 if none fit.
     */
    public static int getSlotInOpenContainer(String name) {
        Container openContainer = Ref.player().openContainer;
        if(openContainer == null) return -1;
        for(int i = 0; i < openContainer.inventorySlots.size()+2-2; i++) {
            Slot slot = openContainer.getSlot(i);
            if(slot == null) continue;
            ItemStack stack = slot.getStack();
            if(stack == null) continue;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).contains(name))
                return i;
        }
        return -1;
    }

    /**
     * Get the slot number of an item which display name is exactly the given name in the currently open container / gui.
     *
     * @param name The name you want to find without color codes.
     * @return The first slot number that has the given name or -1 if none fit.
     */
    public static int getSlotInOpenContainerExact(String name) {
        Container openContainer = Ref.player().openContainer;
        if(openContainer == null) return -1;
        for(int i = 0; i < openContainer.inventorySlots.size()+1-1; i++) {
            Slot slot = openContainer.getSlot(i);
            if(slot == null) continue;
            ItemStack stack = slot.getStack();
            if(stack == null) continue;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).equals(name))
                return i;
        }
        return -1;
    }

    /**
     * Check if an item which display name contains the given name is in the player's inventory.
     *
     * @param name Part of the name you want to find without control codes.
     * @return Whether there is an item which name contains given string.
     */
    public static boolean isInInventory(String name) {
        return getInInventory(name) != null;
    }

    /**
     * Check if an item which display name is the given name is in the player's inventory.
     *
     * @param name The name you want to find without control codes.
     * @return Whether there is an item which name is the given string.
     */
    public static boolean isInInventoryExact(String name) {
        return getInInventoryExact(name) != null;
    }

    /**
     * Get an ItemStack which display name contains the given name in the player's inventory.
     *
     * @param name Part of the name you want to find without color codes.
     * @return The first ItemStack that contains given name or null if none fit.
     */
    public static @Nullable ItemStack getInInventory(String name) {
        for(int i = 9; i < 36; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if(stack == null) return null;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).contains(name))
                return stack;
        }
        return null;
    }

    /**
     * Get an ItemStack which display name is exactly the given name in the player's inventory.
     *
     * @param name The name you want to find without color codes.
     * @return The first ItemStack that has the given name or null if none fit.
     */
    public static @Nullable ItemStack getInInventoryExact(String name) {
        for(int i = 9; i < 36; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if(stack == null) return null;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).equals(name))
                return stack;
        }
        return null;
    }

    /**
     * Get the slot number of an item which display name contains the given name in the player's inventory.
     *
     * @param name Part of the name you want to find without color codes.
     * @return The first slot number that contains given name or -1 if none fit.
     */
    public static int getSlotInInventory(String name) {
        for(int i = 9; i < 36; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if(stack == null) return -1;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).contains(name))
                return i;
        }
        return -1;
    }

    /**
     * Get the slot number of an item which display name is exactly the given name in the player's inventory.
     *
     * @param name The name you want to find without color codes.
     * @return The first slot number that has the given name or -1 if none fit.
     */
    public static int getSlotInInventoryExact(String name) {
        for(int i = 9; i < 36; i++) {
            ItemStack stack = Ref.player().inventory.getStackInSlot(i);
            if(stack == null) return -1;
            if(StringUtils.stripControlCodes(stack.getDisplayName()).equals(name))
                return i;
        }
        return -1;
    }
}