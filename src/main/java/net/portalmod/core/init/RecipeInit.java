package net.portalmod.core.init;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portalgun.PortalGunDuplicateRecipe;
import net.portalmod.common.sorted.portalgun.PortalGunModifyRecipe;

public class RecipeInit {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PortalMod.MODID);

    public static final RegistryObject<SpecialRecipeSerializer<PortalGunModifyRecipe>> PORTAL_GUN_MODIFY = RECIPES.register("portalgun_modifying", () -> new SpecialRecipeSerializer<>(PortalGunModifyRecipe::new));
    public static final RegistryObject<SpecialRecipeSerializer<PortalGunDuplicateRecipe>> PORTAL_GUN_DUPLICATE = RECIPES.register("portalgun_duplicating", () -> new SpecialRecipeSerializer<>(PortalGunDuplicateRecipe::new));
}
