package net.portalmod.core.init;

import net.portalmod.core.registry.RegistryObject;
import net.portalmod.core.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portalgun.PortalGunDuplicateRecipe;
import net.portalmod.common.sorted.portalgun.PortalGunModifyRecipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class RecipeInit {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, PortalMod.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<PortalGunModifyRecipe>> PORTAL_GUN_MODIFY = RECIPES.register("portalgun_modifying", () -> new SimpleCraftingRecipeSerializer<>(PortalGunModifyRecipe::new));
    public static final RegistryObject<SimpleCraftingRecipeSerializer<PortalGunDuplicateRecipe>> PORTAL_GUN_DUPLICATE = RECIPES.register("portalgun_duplicating", () -> new SimpleCraftingRecipeSerializer<>(PortalGunDuplicateRecipe::new));
}
