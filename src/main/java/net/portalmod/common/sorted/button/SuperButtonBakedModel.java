package net.portalmod.common.sorted.button;

import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.portalmod.core.math.BiHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class SuperButtonBakedModel implements IDynamicBakedModel {
    private final BiHashMap<Direction, QuadBlockCorner, BakedModel> variants = new BiHashMap<>();
    private final BakedModel base;
    
    public SuperButtonBakedModel(BakedModel base) {
        this.base = base;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        BakedModel variant = this.variants.get(state.getValue(SuperButtonBlock.FACING), state.getValue(SuperButtonBlock.CORNER));
        if(variant == null)
            return new ArrayList<>();
        return variant.getQuads(state, side, rand);
    }
    
    public void addVariant(Direction facing, QuadBlockCorner corner, BakedModel variant) {
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
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}