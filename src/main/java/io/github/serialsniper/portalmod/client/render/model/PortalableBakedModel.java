package io.github.serialsniper.portalmod.client.render.model;

import com.google.common.collect.*;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.state.properties.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.client.model.data.*;
import net.minecraftforge.client.model.pipeline.*;

import javax.annotation.*;
import java.util.*;

public class PortalableBakedModel implements IDynamicBakedModel {
    // todo cache model

    private final ItemCameraTransforms itemCameraTransforms;

    // block itself
    public static final ResourceLocation BASE = new ResourceLocation(PortalMod.MODID, "blocks/portalable_block/base");
    public static final ResourceLocation CUTOUT_TOP = new ResourceLocation(PortalMod.MODID, "blocks/portalable_block/cutout_top");
    public static final ResourceLocation CUTOUT_BOTTOM = new ResourceLocation(PortalMod.MODID, "blocks/portalable_block/cutout_bottom");

    // portals
    public static final ResourceLocation PORTAL_MASK = new ResourceLocation(PortalMod.MODID, "textures/portals/mask.png");
    public static final ResourceLocation HIGHLIGHT_BLUE = new ResourceLocation(PortalMod.MODID, "textures/portals/highlight/blue.png");
    public static final ResourceLocation HIGHLIGHT_ORANGE = new ResourceLocation(PortalMod.MODID, "textures/portals/highlight/orange.png");

    public static final ResourceLocation CLOSED_BLUE_TOP = new ResourceLocation(PortalMod.MODID, "portals/closed/blue/top");
    public static final ResourceLocation CLOSED_BLUE_BOTTOM = new ResourceLocation(PortalMod.MODID, "portals/closed/blue/bottom");
    public static final ResourceLocation CLOSED_ORANGE_TOP = new ResourceLocation(PortalMod.MODID, "portals/closed/orange/top");
    public static final ResourceLocation CLOSED_ORANGE_BOTTOM = new ResourceLocation(PortalMod.MODID, "portals/closed/orange/bottom");

    public static final ResourceLocation OPEN_BLUE_TOP = new ResourceLocation(PortalMod.MODID, "portals/open/blue/top");
    public static final ResourceLocation OPEN_BLUE_BOTTOM = new ResourceLocation(PortalMod.MODID, "portals/open/blue/bottom");
    public static final ResourceLocation OPEN_ORANGE_TOP = new ResourceLocation(PortalMod.MODID, "portals/open/orange/top");
    public static final ResourceLocation OPEN_ORANGE_BOTTOM = new ResourceLocation(PortalMod.MODID, "portals/open/orange/bottom");

    public PortalableBakedModel(ItemCameraTransforms itemCameraTransforms) {
        this.itemCameraTransforms = itemCameraTransforms;
    }

    public static List<ResourceLocation> getAllTextures() {
        List<ResourceLocation> textures = new ArrayList<>();

        textures.add(BASE);
        textures.add(CUTOUT_TOP);
        textures.add(CUTOUT_BOTTOM);

        textures.add(CLOSED_BLUE_TOP);
        textures.add(CLOSED_BLUE_BOTTOM);
        textures.add(CLOSED_ORANGE_TOP);
        textures.add(CLOSED_ORANGE_BOTTOM);

        textures.add(OPEN_BLUE_TOP);
        textures.add(OPEN_BLUE_BOTTOM);
        textures.add(OPEN_ORANGE_TOP);
        textures.add(OPEN_ORANGE_BOTTOM);

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
        double l = 0;
        double r = 1;

        switch(side) {
            case UP:    quads.add(createQuad(v(l, r, l), v(l, r, r), v(r, r, r), v(r, r, l), texture)); break;
            case DOWN:  quads.add(createQuad(v(l, l, r), v(l, l, l), v(r, l, l), v(r, l, r), texture)); break;
            case EAST:  quads.add(createQuad(v(r, r, r), v(r, l, r), v(r, l, l), v(r, r, l), texture)); break;
            case WEST:  quads.add(createQuad(v(l, r, l), v(l, l, l), v(l, l, r), v(l, r, r), texture)); break;
            case NORTH: quads.add(createQuad(v(r, r, l), v(r, l, l), v(l, l, l), v(l, r, l), texture)); break;
            case SOUTH: quads.add(createQuad(v(l, r, r), v(l, l, r), v(r, l, r), v(r, r, r), texture)); break;
        }
    }

//    private static BakedQuad transformQuad(BakedQuad quad, float light) {
//        VertexFormat newFormat = RenderUtils.getFormatWithLightMap(quad.getFormat());
//
//        BakedQuadBuilder builder = new BakedQuadBuilder(newFormat);
//
//        VertexLighterFlat trans = new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors()) {
//            @Override
//            protected void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z) {
//                lightmap[0] = light;
//                lightmap[1] = light;
//            }
//
//            @Override
//            public void setQuadTint(int tint) {
//                // NO OP
//            }
//        };
//
//        trans.setParent(builder);
//
//        quad.pipe(trans);
//
//        builder.setQuadTint(quad.getTintIndex());
//        builder.setQuadOrientation(quad.getFace());
//        builder.setTexture(quad.getSprite());
//        builder.setApplyDiffuseLighting(false);
//
//        return builder.build();
//    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
//        if(side == null)
//            return Collections.emptyList();
//
//        List<BakedQuad> quads = new ArrayList<>();
//        ResourceLocation texture;
//
//        switch(side) {
//            case UP:
//            case DOWN:
//                texture = new ResourceLocation("block/furnace_top");
//                break;
//
//            case NORTH:
//                texture = new ResourceLocation("block/furnace_front");
//                break;
//
//            default:
//                texture = new ResourceLocation("block/furnace_side");
//                break;
//        }
//
//        addQuad(quads, side, Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture));
//        return quads;

        List<BakedQuad> quads = new ArrayList<>();
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(BASE);

        if(state == null && side != null) {
            addQuad(quads, side, texture);
            return quads;
        }

        if(side == null
            || (state.getValue(PortalableBlock.ACTIVE) && state.getValue(PortalableBlock.HALF) == DoubleBlockHalf.UPPER && side == Direction.DOWN)
            || (state.getValue(PortalableBlock.ACTIVE) && state.getValue(PortalableBlock.HALF) == DoubleBlockHalf.LOWER && side == Direction.UP))
            return Collections.emptyList();

        TextureAtlasSprite overlayTexture = null;

        if(state.getValue(PortalableBlock.ACTIVE)) {
            if(side == state.getValue(PortalableBlock.FACING)) {
                if(state.getValue(PortalableBlock.HALF) == DoubleBlockHalf.UPPER) {
                    if(state.getValue(PortalableBlock.END) == PortalEnd.BLUE) {
                        overlayTexture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(OPEN_BLUE_TOP);
                    } else {
                        overlayTexture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(OPEN_ORANGE_TOP);
                    }

                    texture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(CUTOUT_TOP);
                } else {
                    if(state.getValue(PortalableBlock.END) == PortalEnd.BLUE) {
                        overlayTexture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(OPEN_BLUE_BOTTOM);
                    } else {
                        overlayTexture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(OPEN_ORANGE_BOTTOM);
                    }

                    texture = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(CUTOUT_BOTTOM);
                }
            }
        }

        addQuad(quads, side, texture);

        if(overlayTexture != null)
            addQuad(quads, side, overlayTexture);

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
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(BASE);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(BASE);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return itemCameraTransforms;
    }

    static class PortalableBakedQuadBuilder implements IVertexConsumer {
        private static final int SIZE = DefaultVertexFormats.BLOCK.getElements().size();

        private final float[][][] unpackedData = new float[4][SIZE][4];
        private int tint = -1;
        private Direction orientation;
        private TextureAtlasSprite texture;
        private boolean applyDiffuseLighting = true;

        private int vertices = 0;
        private int elements = 0;
        private boolean full = false;
        private boolean contractUVs = false;

        public PortalableBakedQuadBuilder() {}

        public PortalableBakedQuadBuilder(TextureAtlasSprite texture) {
            this.texture = texture;
        }

        public void setContractUVs(boolean value) {
            this.contractUVs = value;
        }

        @Override
        public VertexFormat getVertexFormat() {
            return DefaultVertexFormats.BLOCK;
        }

        @Override
        public void setQuadTint(int tint) {
            this.tint = tint;
        }

        @Override
        public void setQuadOrientation(Direction orientation) {
            this.orientation = orientation;
        }

        @Override
        public void setTexture(TextureAtlasSprite texture) {
            this.texture = texture;
        }

        @Override
        public void setApplyDiffuseLighting(boolean diffuse) {
            this.applyDiffuseLighting = diffuse;
        }

        @Override
        public void put(int element, float... data) {
            for (int i = 0; i < 4; i++) {
                if (i < data.length) {
                    unpackedData[vertices][element][i] = data[i];
                } else {
                    unpackedData[vertices][element][i] = 0;
                }
            }
            elements++;
            if (elements == SIZE) {
                vertices++;
                elements = 0;
            }
            if (vertices == 4) {
                full = true;
            }
        }

        private final float eps = 1f / 0x100;

        public BakedQuad build() {
            if(!full)
                throw new IllegalStateException("not enough data");
            if(texture == null)
                throw new IllegalStateException("texture not set");
            if(contractUVs) {
                float tX = texture.getWidth() / (texture.getU1() - texture.getU0());
                float tY = texture.getHeight() / (texture.getV1() - texture.getV0());
                float tS = tX > tY ? tX : tY;
                float ep = 1f / (tS * 0x100);
                int uve = 0;
                ImmutableList<VertexFormatElement> elements = DefaultVertexFormats.BLOCK.getElements();
                while(uve < elements.size()) {
                    VertexFormatElement e = elements.get(uve);
                    if(e.getUsage() == VertexFormatElement.Usage.UV && e.getIndex() == 0)
                        break;
                    uve++;
                }
                if(uve == elements.size())
                    throw new IllegalStateException("Can't contract UVs: format doesn't contain UVs");
                float[] uvc = new float[4];
                for(int v = 0; v < 4; v++)
                    for (int i = 0; i < 4; i++)
                        uvc[i] += unpackedData[v][uve][i] / 4;
                for(int v = 0; v < 4; v++) {
                    for(int i = 0; i < 4; i++) {
                        float uo = unpackedData[v][uve][i];
                        float un = uo * (1 - eps) + uvc[i] * eps;
                        float ud = uo - un;
                        float aud = ud;
                        if(aud < 0) aud = -aud;
                        if(aud < ep) {
                            float udc = uo - uvc[i];
                            if(udc < 0) udc = -udc;
                            if(udc < 2 * ep) {
                                un = (uo + uvc[i]) / 2;
                            } else {
                                un = uo + (ud < 0 ? ep : -ep);
                            }
                        }
                        unpackedData[v][uve][i] = un;
                    }
                }
            }
            int[] packed = new int[DefaultVertexFormats.BLOCK.getIntegerSize() * 4];
            for(int v = 0; v < 4; v++)
                for(int e = 0; e < SIZE; e++)
                    LightUtil.pack(unpackedData[v][e], packed, DefaultVertexFormats.BLOCK, v, e);
            return new BakedQuad(packed, tint, orientation, texture, applyDiffuseLighting);
        }
    }
}