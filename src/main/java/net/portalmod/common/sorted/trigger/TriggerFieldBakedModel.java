package net.portalmod.common.sorted.trigger;

import net.portalmod.client.render.ModQuadBuilder;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;


import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Builds the textured quads the trigger TER draws (not a block model). */
public class TriggerFieldBakedModel {
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

    private void putVertex(ModQuadBuilder builder, Vec3 normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {
        builder.vertex(x, y, z, sprite.getU(u), sprite.getV(v), r, g, b, normal);
    }

    private BakedQuad createQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, TextureAtlasSprite sprite) {
        Vec3 normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();
        int tw = sprite.contents().width();
        int th = sprite.contents().height();

        ModQuadBuilder builder = new ModQuadBuilder(sprite);
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

    private BakedQuad genQuad(Direction side, ResourceLocation texture) {
        double l = -0.001;
        double r = 1 + 0.001;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);

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

    public BakedQuad getQuad(Direction side, ResourceLocation texture) {
        return this.bakedQuads.get(texture).get(side);
    }
}