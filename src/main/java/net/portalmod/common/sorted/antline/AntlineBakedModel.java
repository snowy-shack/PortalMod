package net.portalmod.common.sorted.antline;

import java.util.function.Supplier;

import net.minecraft.util.RandomSource;

import net.minecraft.client.resources.model.BakedModel;

import net.minecraft.client.renderer.block.model.ItemTransforms;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

import net.portalmod.client.render.ModQuadBuilder;

import com.google.common.collect.ImmutableList;
import net.portalmod.PortalMod;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Antline block model. The quads depend on the block entity's sides, so this uses the Fabric
 * Rendering API: emitBlockQuads reads the SideMap the block entity provides as render data.
 */
public class AntlineBakedModel implements BakedModel, FabricBakedModel {
    /** Used when the block is rendered before the tile entity exists (e.g. chunk build race). */
    /** Used when the block is rendered before the block entity exists (e.g. chunk build race). */
    private static final AntlineTileEntity.SideMap DEFAULT_SIDES = new AntlineTileEntity.SideMap();

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

    private void putVertex(ModQuadBuilder builder, net.minecraft.world.phys.Vec3 normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {
        builder.vertex(x, y, z, sprite.getU(u), sprite.getV(v), r, g, b, normal);
    }

    private BakedQuad createQuad(net.minecraft.world.phys.Vec3 vec1, net.minecraft.world.phys.Vec3 vec2, net.minecraft.world.phys.Vec3 vec3, net.minecraft.world.phys.Vec3 vec4, TextureAtlasSprite sprite) {
        net.minecraft.world.phys.Vec3 normal = vec3.subtract(vec2).cross(vec1.subtract(vec2)).normalize();
//        int tw = sprite.getWidth() + (int)(offset.x / 16f);
//        int th = sprite.getHeight() + (int)(offset.y / 16f);

//        int u0 = xOffs;
//        int v0 = yOffs;
//        int u1 = sprite.getWidth() + xOffs;
//        int v1 = sprite.getHeight() + yOffs;

        int u0 = 0;
        int v0 = 0;
        int u1 = sprite.contents().width();
        int v1 = sprite.contents().height();

        ModQuadBuilder builder = new ModQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x, normal.y, normal.z));
        putVertex(builder, normal, vec1.x, vec1.y, vec1.z, u0, v0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, vec2.x, vec2.y, vec2.z, u0, v1, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, vec3.x, vec3.y, vec3.z, u1, v1, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, vec4.x, vec4.y, vec4.z, u1, v0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private net.minecraft.world.phys.Vec3 v(double x, double y, double z) {
        return new net.minecraft.world.phys.Vec3(x, y, z);
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

        net.minecraft.world.phys.Vec3 absOffset = new Vec3(offset.getNormal()).transform(relToAbs).mul(5 / 16f).to3d();
        if (offset.getAxis().isVertical())
            absOffset = net.minecraft.world.phys.Vec3.ZERO;

        TextureAtlasSprite tex = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);

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

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        Object data = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        AntlineTileEntity.SideMap sideMap = data instanceof AntlineTileEntity.SideMap map ? map : DEFAULT_SIDES;
        QuadEmitter emitter = context.getEmitter();
        for (BakedQuad quad : buildQuads(sideMap)) {
            emitter.fromVanilla(quad, null, null);
            emitter.emit();
        }
    }

    /** Vanilla path (no block entity data available): nothing to draw. */
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction sideDir, RandomSource rand) {
        return Collections.emptyList();
    }

    private List<BakedQuad> buildQuads(AntlineTileEntity.SideMap sideMap) {
        List<BakedQuad> quads = new ArrayList<>();

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
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(inactive("inactive_dot"));
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}