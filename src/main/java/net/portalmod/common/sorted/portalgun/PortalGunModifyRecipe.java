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

public class PortalGunModifyRecipe extends SpecialRecipe {
    public static final Ingredient CHAIN_INGREDIENT = Ingredient.of(Items.CHAIN);

    public PortalGunModifyRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
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
        boolean hasChain = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty() && i != 4) {
                hasModifier = true;

                if ((i == 1 || i == 3 || i == 5)) {
                    if (item.getItem() instanceof DyeItem) continue; // Check dyes
                    if (CHAIN_INGREDIENT.test(item) && !hasChain) { // Check chain
                        hasChain = true;
                        continue;
                    }
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

        if (!accentDye.isEmpty())
                newNBT.putString("AccentColor", ((DyeItem) accentDye.getItem()).getDyeColor().getName());

        if (!leftDye.isEmpty() && leftDye.getItem() instanceof DyeItem)
                newNBT.putString("LeftColor", ((DyeItem) leftDye.getItem()).getDyeColor().getName());
        if (!rightDye.isEmpty() && rightDye.getItem() instanceof DyeItem)
                newNBT.putString("RightColor", ((DyeItem) rightDye.getItem()).getDyeColor().getName());

        String lock = "None";
        if (!leftDye.isEmpty() && CHAIN_INGREDIENT.test(leftDye)) lock = "Left";
        if (!rightDye.isEmpty() && CHAIN_INGREDIENT.test(rightDye)) lock = "Right";

        newNBT.putString("Locked", lock);
        newNBT.putInt("LastPortal", 0);

        return newGun;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // Only works in 3x3 for now
        return width == 3 && height == 3;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeInit.PORTAL_GUN_MODIFY.get();
    }
}
