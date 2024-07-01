package net.portalmod.common.sorted.portalgun;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.portalmod.core.init.RecipeInit;

public class PortalGunRecipe extends SpecialRecipe {
    public static final Ingredient CHAIN_INGREDIENT = Ingredient.of(Items.CHAIN);

    public PortalGunRecipe(ResourceLocation p_i48169_1_) {
        super(p_i48169_1_);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        // Check gun
        ItemStack gunItem = inventory.getItem(4);
        if (gunItem.isEmpty() || !(gunItem.getItem() instanceof PortalGun)) {
            return false;
        }

        // Whether another item besides the gun is present
        boolean hasModifier = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty() && i != 4) {
                hasModifier = true;

                // Check dyes
                if ((i == 1 || i == 3 || i == 5) && item.getItem() instanceof DyeItem) {
                    continue;
                }

                // Check chain
                if (i == 7 && CHAIN_INGREDIENT.test(item)) {
                    continue;
                }

                return false;
            }
        }

        return hasModifier;
    }

    @Override
    public ItemStack assemble(CraftingInventory inventory) {
        ItemStack newGun = inventory.getItem(4).copy();
        CompoundNBT newNBT = newGun.getOrCreateTag();

        ItemStack accentDye = inventory.getItem(1);
        ItemStack leftDye = inventory.getItem(3);
        ItemStack rightDye = inventory.getItem(5);
        ItemStack chain = inventory.getItem(7);

        if (!accentDye.isEmpty()) {
            newNBT.putString("AccentColor", ((DyeItem) accentDye.getItem()).getDyeColor().getName());
        }
        if (!leftDye.isEmpty()) {
            newNBT.putString("LeftColor", ((DyeItem) leftDye.getItem()).getDyeColor().getName());
        }
        if (!rightDye.isEmpty()) {
            newNBT.putString("RightColor", ((DyeItem) rightDye.getItem()).getDyeColor().getName());
        }

        newNBT.putBoolean("Locked", !chain.isEmpty());

        return newGun;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // Only works in 3x3 for now
        return width == 3 && height == 3;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeInit.PORTAL_GUN.get();
    }
}
