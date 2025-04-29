package net.portalmod.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.ItemInit;

import java.util.function.Consumer;

public class RecipeGen extends RecipeProvider {
    public RecipeGen(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(BlockInit.ANTLINE.get(), 4)
                .requires(Items.SLIME_BALL)
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_item", RecipeProvider.has(ItemInit.SUPER_BUTTON.get()))
                .save(consumer);
    }
}
