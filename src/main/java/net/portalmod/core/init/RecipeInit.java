package net.portalmod.core.init;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portalgun.PortalGunRecipe;

public class RecipeInit {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PortalMod.MODID);

    public static final RegistryObject<SpecialRecipeSerializer<PortalGunRecipe>> PORTAL_GUN = RECIPES.register("portalgun_modifying", () -> new SpecialRecipeSerializer<>(PortalGunRecipe::new));
}
