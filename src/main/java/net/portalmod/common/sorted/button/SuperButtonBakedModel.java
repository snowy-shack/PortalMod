package net.portalmod.common.sorted.button;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.portalmod.core.math.BiHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SuperButtonBakedModel implements IDynamicBakedModel {
    private final BiHashMap<Direction, QuadBlockCorner, IBakedModel> variants = new BiHashMap<>();
    private final IBakedModel base;
    
    public SuperButtonBakedModel(IBakedModel base) {
        this.base = base;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        IBakedModel variant = this.variants.get(state.getValue(SuperButtonBlock.FACING), state.getValue(SuperButtonBlock.CORNER));
        if(variant == null)
            return new ArrayList<>();
        return variant.getQuads(state, side, rand);
    }
    
    public void addVariant(Direction facing, QuadBlockCorner corner, IBakedModel variant) {
        this.variants.put(facing, corner, variant);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean isCustomRenderer() {
        return base.isCustomRenderer();
    }

    @Override
    public boolean usesBlockLight() {
        return base.usesBlockLight();
    }

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}