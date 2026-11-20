package net.portalmod.common.sorted.faithplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.portalmod.core.init.TextureInit;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class FaithPlateTargetBakedModel implements IDynamicBakedModel {
//    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "misc/faithplate_target");
//    public static final ResourceLocation TEXTURE_FLAT = new ResourceLocation(PortalMod.MODID, "misc/faithplate_target_flat");

//    public static List<ResourceLocation> getAllTextures() {
//        List<ResourceLocation> textures = new ArrayList<>();
//        textures.add(TEXTURE);
//        textures.add(TEXTURE_FLAT);
//        return textures;
//    }

    private void putVertex(BakedQuadBuilder builder, Vec3 normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {

        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
        for (int j = 0 ; j < elements.size() ; j++) {
            VertexFormatElement e = elements.get(j);
            switch (e.getUsage()) {
                case POSITION:
                    builder.put(j, (float) x, (float) y, (float) z, 1.0f);
                    break;
                case COLOR:
                    builder.put(j, r, g, b, 1.0f);
                    break;
                case UV:
                    switch (e.getIndex()) {
                        case 0:
                            float iu = sprite.getU(u);
                            float iv = sprite.getV(v);
                            builder.put(j, iu, iv);
                            break;
                        case 2:
                            builder.put(j, (short) 0, (short) 0);
                            break;
                        default:
                            builder.put(j);
                            break;
                    }
                    break;
                case NORMAL:
                    builder.put(j, (float) normal.x, (float) normal.y, (float) normal.z);
                    break;
                default:
                    builder.put(j);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, TextureAtlasSprite sprite) {
        Vec3 normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();
        int tw = sprite.getWidth();
        int th = sprite.getHeight();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, th, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, tw, th, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, tw, 0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private Vec3 v(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    private void addQuad(List<BakedQuad> quads, Direction side, TextureAtlasSprite texture, float offset) {
        double l = -offset;
        double r = 1 + offset;

        switch(side) {
            case UP:    quads.add(createQuad(v(l, r, l), v(l, r, r), v(r, r, r), v(r, r, l), texture)); break;
            case DOWN:  quads.add(createQuad(v(l, l, r), v(l, l, l), v(r, l, l), v(r, l, r), texture)); break;
            case EAST:  quads.add(createQuad(v(r, r, r), v(r, l, r), v(r, l, l), v(r, r, l), texture)); break;
            case WEST:  quads.add(createQuad(v(l, r, l), v(l, l, l), v(l, l, r), v(l, r, r), texture)); break;
            case NORTH: quads.add(createQuad(v(r, r, l), v(r, l, l), v(l, l, l), v(l, r, l), texture)); break;
            case SOUTH: quads.add(createQuad(v(l, r, r), v(l, l, r), v(r, l, r), v(r, r, r), texture)); break;
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return Collections.emptyList();
    }

    public BakedQuad getQuad(Direction side, float offset) {
        List<BakedQuad> quads = new ArrayList<>();
        addQuad(quads, side, Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(TextureInit.FAITHPLATE_TARGET), offset);
        return quads.get(0);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TextureInit.FAITHPLATE_TARGET);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(TextureInit.FAITHPLATE_TARGET);
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}