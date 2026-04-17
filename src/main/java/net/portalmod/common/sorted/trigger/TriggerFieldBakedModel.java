package net.portalmod.common.sorted.trigger;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

public class TriggerFieldBakedModel implements IDynamicBakedModel {
    private final Map<ResourceLocation, Map<Direction, BakedQuad>> bakedQuads;

    public TriggerFieldBakedModel() {
        this.bakedQuads = new HashMap<>();
    }

    public void bakeQuadsOnce() {
        if(this.bakedQuads.isEmpty()) {
            for(ResourceLocation texture : TriggerTER.getAllFieldTextures()) {
                Map<Direction, BakedQuad> perDirection = new HashMap<>();
                for(Direction direction : Direction.values()) {
                    perDirection.put(direction, this.genQuad(direction, texture));
                }
                bakedQuads.put(texture, perDirection);
            }
        }
    }

    private void putVertex(BakedQuadBuilder builder, Vector3d normal,
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

    private BakedQuad createQuad(Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4, TextureAtlasSprite sprite) {
        Vector3d normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();
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

    private Vector3d v(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    private BakedQuad genQuad(Direction side, ResourceLocation texture) {
        double l = -0.001;
        double r = 1 + 0.001;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture);

        switch(side) {
            case UP:    return createQuad(v(0, r, 0), v(0, r, 1), v(1, r, 1), v(1, r, 0), sprite);
            case DOWN:  return createQuad(v(0, l, 1), v(0, l, 0), v(1, l, 0), v(1, l, 1), sprite);
            case EAST:  return createQuad(v(r, 1, 1), v(r, 0, 1), v(r, 0, 0), v(r, 1, 0), sprite);
            case WEST:  return createQuad(v(l, 1, 0), v(l, 0, 0), v(l, 0, 1), v(l, 1, 1), sprite);
            case NORTH: return createQuad(v(1, 1, l), v(1, 0, l), v(0, 0, l), v(0, 1, l), sprite);
            case SOUTH: return createQuad(v(0, 1, r), v(0, 0, r), v(1, 0, r), v(1, 1, r), sprite);
        }

        return null;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return Collections.emptyList();
    }

    public BakedQuad getQuad(Direction side, ResourceLocation texture) {
        return this.bakedQuads.get(texture).get(side);
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
        return null;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}