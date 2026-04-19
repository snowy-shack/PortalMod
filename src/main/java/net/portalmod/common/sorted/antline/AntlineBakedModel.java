package net.portalmod.common.sorted.antline;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.portalmod.PortalMod;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AntlineBakedModel implements IDynamicBakedModel {
    /** Used when the block is rendered before the tile entity exists (e.g. chunk build race). */
    private static final IModelData DEFAULT_MODEL_DATA = new AntlineTileEntity.SideMap().toModelData();

//    public static final ResourceLocation ACTIVE_DOT = new ResourceLocation(PortalMod.MODID, "blocks/antline/active_dot");
//    public static final ResourceLocation ACTIVE_CORNER = new ResourceLocation(PortalMod.MODID, "blocks/antline/active_corner");
//    public static final ResourceLocation INACTIVE_DOT = new ResourceLocation(PortalMod.MODID, "blocks/antline/inactive_dot");
//    public static final ResourceLocation INACTIVE_CORNER = new ResourceLocation(PortalMod.MODID, "blocks/antline/inactive_corner");

    public static ResourceLocation active(String string) {
        return new ResourceLocation(PortalMod.MODID, "antline/" + string);
    }

    public static ResourceLocation inactive(String string) {
        return new ResourceLocation(PortalMod.MODID, "antline/" + string);
    }

    public static List<ResourceLocation> getAllTextures() {
        List<ResourceLocation> textures = new ArrayList<>();

        textures.add(new ResourceLocation(PortalMod.MODID, "antline/active_dot"));
        textures.add(new ResourceLocation(PortalMod.MODID, "antline/active_corner"));
        textures.add(new ResourceLocation(PortalMod.MODID, "antline/inactive_dot"));
        textures.add(new ResourceLocation(PortalMod.MODID, "antline/inactive_corner"));

//        textures.add(active("dot"));
//        textures.add(active("corner"));
//        textures.add(active("north"));
//        textures.add(active("east"));
//        textures.add(active("south"));
//        textures.add(active("west"));
//        textures.add(active("particle"));
//
//        textures.add(inactive("dot"));
//        textures.add(inactive("corner"));
//        textures.add(inactive("north"));
//        textures.add(inactive("east"));
//        textures.add(inactive("south"));
//        textures.add(inactive("west"));
//        textures.add(inactive("particle"));

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

    private BakedQuad createQuad(Vector3d vec1, Vector3d vec2, Vector3d vec3, Vector3d vec4, TextureAtlasSprite sprite) {
        Vector3d normal = vec3.subtract(vec2).cross(vec1.subtract(vec2)).normalize();
//        int tw = sprite.getWidth() + (int)(offset.x / 16f);
//        int th = sprite.getHeight() + (int)(offset.y / 16f);

//        int u0 = xOffs;
//        int v0 = yOffs;
//        int u1 = sprite.getWidth() + xOffs;
//        int v1 = sprite.getHeight() + yOffs;

        int u0 = 0;
        int v0 = 0;
        int u1 = sprite.getWidth();
        int v1 = sprite.getHeight();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x, normal.y, normal.z));
        putVertex(builder, normal, vec1.x, vec1.y, vec1.z, u0, v0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, vec2.x, vec2.y, vec2.z, u0, v1, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, vec3.x, vec3.y, vec3.z, u1, v1, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, vec4.x, vec4.y, vec4.z, u1, v0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private Vector3d v(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    private void addQuad(List<BakedQuad> quads, Direction side, Direction offset, ResourceLocation texture) {
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

        Vec3 y = new Vec3((side == Direction.UP ? side : side.getOpposite()).getNormal());
        Vec3 z = new Vec3((side.getAxis().isHorizontal() ? Direction.DOWN : Direction.SOUTH).getNormal());
        Vec3 x = y.clone().cross(z);

        Mat4 relToAbs = new Mat4(
                x.x, y.x, z.x, 0,
                x.y, y.y, z.y, 0,
                x.z, y.z, z.z, 0,
                0, 0, 0, 1
        );

        Vector3d absOffset = new Vec3(offset.getNormal()).transform(relToAbs).mul(5 / 16f).to3d();
        if (offset.getAxis().isVertical())
            absOffset = Vector3d.ZERO;

        TextureAtlasSprite tex = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture);

        switch(side) {
            case UP:    quads.add(createQuad(v(ux, u, dy).add(absOffset), v(ux, u, uy).add(absOffset), v(dx, u, uy).add(absOffset), v(dx, u, dy).add(absOffset), tex)); break;
            case DOWN:  quads.add(createQuad(v(dx, d, dy).add(absOffset), v(dx, d, uy).add(absOffset), v(ux, d, uy).add(absOffset), v(ux, d, dy).add(absOffset), tex)); break;
            case EAST:  quads.add(createQuad(v(u, uy, dx).add(absOffset), v(u, dy, dx).add(absOffset), v(u, dy, ux).add(absOffset), v(u, uy, ux).add(absOffset), tex)); break;
            case WEST:  quads.add(createQuad(v(d, uy, ux).add(absOffset), v(d, dy, ux).add(absOffset), v(d, dy, dx).add(absOffset), v(d, uy, dx).add(absOffset), tex)); break;
            case NORTH: quads.add(createQuad(v(dx, uy, d).add(absOffset), v(dx, dy, d).add(absOffset), v(ux, dy, d).add(absOffset), v(ux, uy, d).add(absOffset), tex)); break;
            case SOUTH: quads.add(createQuad(v(ux, uy, u).add(absOffset), v(ux, dy, u).add(absOffset), v(dx, dy, u).add(absOffset), v(dx, uy, u).add(absOffset), tex)); break;
        }

        switch(side) {
            case UP:    quads.add(createQuad(v(dx, u, dy).add(absOffset), v(dx, u, uy).add(absOffset), v(ux, u, uy).add(absOffset), v(ux, u, dy).add(absOffset), tex)); break;
            case DOWN:  quads.add(createQuad(v(ux, d, dy).add(absOffset), v(ux, d, uy).add(absOffset), v(dx, d, uy).add(absOffset), v(dx, d, dy).add(absOffset), tex)); break;
            case EAST:  quads.add(createQuad(v(u, uy, ux).add(absOffset), v(u, dy, ux).add(absOffset), v(u, dy, dx).add(absOffset), v(u, uy, dx).add(absOffset), tex)); break;
            case WEST:  quads.add(createQuad(v(d, uy, dx).add(absOffset), v(d, dy, dx).add(absOffset), v(d, dy, ux).add(absOffset), v(d, uy, ux).add(absOffset), tex)); break;
            case NORTH: quads.add(createQuad(v(ux, uy, d).add(absOffset), v(ux, dy, d).add(absOffset), v(dx, dy, d).add(absOffset), v(dx, uy, d).add(absOffset), tex)); break;
            case SOUTH: quads.add(createQuad(v(dx, uy, u).add(absOffset), v(dx, dy, u).add(absOffset), v(ux, dy, u).add(absOffset), v(ux, uy, u).add(absOffset), tex)); break;
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof AntlineTileEntity) {
            return te.getModelData();
        }
        return DEFAULT_MODEL_DATA;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction sideDir, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> quads = new ArrayList<>();

        if (sideDir != null) return Collections.emptyList();

        AntlineTileEntity.SideMap sideMap = extraData.getData(AntlineTileEntity.SideMap.MODEL_PROPERTY);
        if (sideMap == null) {
            return Collections.emptyList();
        }

        sideMap.forEach((direction, side) -> {
            if (!side.isEmpty()) {
                String path = side.isActive() ? "antline/active_" : "antline/inactive_";

                // Center dot
                if (side.getSideType() == AntlineTileEntity.Side.SideType.NORMAL)
                    addQuad(quads, direction, Direction.UP, new ResourceLocation(PortalMod.MODID, path + "dot"));
                else if (side.getSideType() == AntlineTileEntity.Side.SideType.CORNER)
                    addQuad(quads, direction, Direction.UP, new ResourceLocation(PortalMod.MODID, path + "corner"));

                // Connection dots
                side.getConnections().forEach((connectionDir, b) -> {
                    if (b) addQuad(quads, direction, connectionDir, new ResourceLocation(PortalMod.MODID, path + "dot"));
                });
            }
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
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(inactive("inactive_dot"));
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}