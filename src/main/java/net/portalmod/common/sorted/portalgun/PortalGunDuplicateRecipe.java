package net.portalmod.common.sorted.portalgun;

import net.minecraft.world.item.crafting.CraftingBookCategory;

import net.portalmod.core.init.RecipeInit;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PortalGunDuplicateRecipe extends CustomRecipe {
    public PortalGunDuplicateRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, category);
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        int guns = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem() instanceof PortalGun) {
                guns++;
            }
        }

        return guns == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                return item.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                nonnulllist.set(i, item.copy());
                return nonnulllist;
            }
        }
        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeInit.PORTAL_GUN_DUPLICATE.get();
    }
}
