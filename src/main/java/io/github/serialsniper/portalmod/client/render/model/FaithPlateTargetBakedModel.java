package io.github.serialsniper.portalmod.client.render.model;

import com.google.common.collect.ImmutableList;
import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FaithPlateTargetBakedModel implements IDynamicBakedModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "misc/faithplate_target");
    public static final ResourceLocation TEXTURE_FLAT = new ResourceLocation(PortalMod.MODID, "misc/faithplate_target_flat");

    public static List<ResourceLocation> getAllTextures() {
        List<ResourceLocation> textures = new ArrayList<>();
        textures.add(TEXTURE);
        textures.add(TEXTURE_FLAT);
        return textures;
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

    private void addQuad(List<BakedQuad> quads, Direction side, TextureAtlasSprite texture) {
        double l = -0.001;
        double r = 1.001;

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
        if(side == null)
            return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();
        addQuad(quads, side, Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS)
                .apply(side.getAxis() == Direction.Axis.Y ? TEXTURE_FLAT : TEXTURE));
        return quads;
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
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(TEXTURE);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(TEXTURE);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}