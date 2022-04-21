package io.github.serialsniper.portalmod.client.render.model;

import com.google.common.collect.ImmutableList;
import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.blockentities.AntlineTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AntlineBakedModel implements IDynamicBakedModel {
//    public static final ResourceLocation ACTIVE_DOT = new ResourceLocation(PortalMod.MODID, "blocks/antline/active_dot");
//    public static final ResourceLocation ACTIVE_CORNER = new ResourceLocation(PortalMod.MODID, "blocks/antline/active_corner");
//    public static final ResourceLocation INACTIVE_DOT = new ResourceLocation(PortalMod.MODID, "blocks/antline/inactive_dot");
//    public static final ResourceLocation INACTIVE_CORNER = new ResourceLocation(PortalMod.MODID, "blocks/antline/inactive_corner");

    public static ResourceLocation active(String string) {
        return new ResourceLocation(PortalMod.MODID, "blocks/antline/active/" + string);
    }

    public static ResourceLocation inactive(String string) {
        return new ResourceLocation(PortalMod.MODID, "blocks/antline/inactive/" + string);
    }

    public static List<ResourceLocation> getAllTextures() {
        List<ResourceLocation> textures = new ArrayList<>();

        textures.add(active("dot"));
        textures.add(active("corner"));
        textures.add(active("north"));
        textures.add(active("east"));
        textures.add(active("south"));
        textures.add(active("west"));
        textures.add(active("particle"));

        textures.add(inactive("dot"));
        textures.add(inactive("corner"));
        textures.add(inactive("north"));
        textures.add(inactive("east"));
        textures.add(inactive("south"));
        textures.add(inactive("west"));
        textures.add(inactive("particle"));

//        ACTIVE.forEach((name, location) -> textures.add(location));
//        INACTIVE.forEach((name, location) -> textures.add(location));

//        textures.add(ACTIVE_DOT);
//        textures.add(ACTIVE_CORNER);
//        textures.add(INACTIVE_DOT);
//        textures.add(INACTIVE_CORNER);

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

    private void addQuad(List<BakedQuad> quads, Direction side, ResourceLocation texture) {
//        double w = 4;
//        double h = 4;
        double d = .001;
        double u = .999;
//        double ux = (1f - w / 16f) / 2f + offsetX / 16f;
//        double dx = 1f - ((1f - w / 16f) / 2f) + offsetX / 16f;
//        double uy = (1f - h / 16f) / 2f + offsetY / 16f;
//        double dy = 1f - ((1f - h / 16f) / 2f) + offsetY / 16f;

        double ux = u;
        double dx = d;
        double uy = u;
        double dy = d;

        TextureAtlasSprite tex = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture);

        switch(side) {
            case UP:    quads.add(createQuad(v(ux, u, dy), v(ux, u, uy), v(dx, u, uy), v(dx, u, dy), tex)); break;
            case DOWN:  quads.add(createQuad(v(dx, d, dy), v(dx, d, uy), v(ux, d, uy), v(ux, d, dy), tex)); break;
            case EAST:  quads.add(createQuad(v(u, uy, dx), v(u, dy, dx), v(u, dy, ux), v(u, uy, ux), tex)); break;
            case WEST:  quads.add(createQuad(v(d, uy, ux), v(d, dy, ux), v(d, dy, dx), v(d, uy, dx), tex)); break;
            case NORTH: quads.add(createQuad(v(dx, uy, d), v(dx, dy, d), v(ux, dy, d), v(ux, uy, d), tex)); break;
            case SOUTH: quads.add(createQuad(v(ux, uy, u), v(ux, dy, u), v(dx, dy, u), v(dx, uy, u), tex)); break;
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return world.getBlockEntity(pos).getModelData();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();

        if(side != null)
            return Collections.emptyList();

        AntlineTileEntity.SideMap sideMap = extraData.getData(AntlineTileEntity.SideMap.MODEL_PROPERTY);

        sideMap.forEach((direction, sideData) -> {
//            HashMap<String, ResourceLocation> current = sideData.isActive() ? ACTIVE : INACTIVE;

            if(sideData.getCenter() == AntlineTileEntity.Side.Center.TRUE)
                addQuad(quads, direction, inactive("dot"));
            else if(sideData.getCenter() == AntlineTileEntity.Side.Center.CORNER)
                addQuad(quads, direction, inactive("corner"));

            sideData.getConnections().forEach((direction1, b) -> {
                if(b) addQuad(quads, direction, inactive(direction1.getName()));
            });
        });

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
        return null;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        // todo somehow different particle
//        PlayerInteractionManager
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(inactive("particle"));
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}