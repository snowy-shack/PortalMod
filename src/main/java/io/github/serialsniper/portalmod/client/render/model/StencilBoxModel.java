package io.github.serialsniper.portalmod.client.render.model;

import com.google.common.collect.*;
import io.github.serialsniper.portalmod.PortalMod;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.client.model.pipeline.*;

import javax.annotation.*;
import java.util.*;

public class StencilBoxModel implements IBakedModel {
    private static final NativeImage IMAGE_TEXTURE = new NativeImage(16, 16, false);
    private static final DynamicTexture TEXTURE = new DynamicTexture(IMAGE_TEXTURE);
    private static final ResourceLocation TEXTURE_LOCATION = Minecraft.getInstance().textureManager.register("stencilblock", TEXTURE);

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        List<BakedQuad> quads = new ArrayList<>();
//        Vector3d[] shape = CoordinateStorage.DOUBLE_PYRAMID_POINTS;
//        if (TEXTURE_SPRITE == null) {
//            TEXTURE_SPRITE = getParticleIcon();
//        }
        Vector3d[][] shape = {
            {
                new Vector3d(1, 1, 0),
                new Vector3d(0, 1, 0),
                new Vector3d(0, 0, 0),
                new Vector3d(1, 0, 0)
            },
            {
                new Vector3d(1, 1, 1),
                new Vector3d(1, 1, 0),
                new Vector3d(1, 0, 0),
                new Vector3d(1, 0, 1)
            },
            {
                new Vector3d(0, 1, 1),
                new Vector3d(1, 1, 1),
                new Vector3d(1, 0, 1),
                new Vector3d(0, 0, 1)
            },
            {
                new Vector3d(0, 1, 0),
                new Vector3d(0, 1, 1),
                new Vector3d(0, 0, 1),
                new Vector3d(0, 0, 0)
            },
            {
                new Vector3d(0, 1, 0),
                new Vector3d(1, 1, 0),
                new Vector3d(1, 1, 1),
                new Vector3d(0, 1, 1)
            },
            {
                new Vector3d(1, 0, 0),
                new Vector3d(0, 0, 0),
                new Vector3d(0, 0, 1),
                new Vector3d(1, 0, 1)
            }
        };

        switch(side) {
            case NORTH:
                quads.add(createQuad(shape[0][0], shape[0][1], shape[0][2], shape[0][3], state));
                break;

            case EAST:
                quads.add(createQuad(shape[1][0], shape[1][1], shape[1][2], shape[1][3], state));
                break;

            case SOUTH:
                quads.add(createQuad(shape[2][0], shape[2][1], shape[2][2], shape[2][3], state));
                break;

            case WEST:
                quads.add(createQuad(shape[3][0], shape[3][1], shape[3][2], shape[3][3], state));
                break;

            case UP:
                quads.add(createQuad(shape[4][0], shape[4][1], shape[4][2], shape[4][3], state));
                break;

            case DOWN:
                quads.add(createQuad(shape[5][0], shape[5][1], shape[5][2], shape[5][3], state));
                break;
        }

        PortalMod.LOGGER.debug(side.toString());

        return quads;
    }

    private BakedQuad createQuad(Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4, BlockState stateIn) {
        Vector3d normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();
        int tw = 1;
        int th = 1;

        BakedQuadBuilder builder = new BakedQuadBuilder();
        builder.setQuadOrientation(Direction.getNearest(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, th, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, tw, th, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, tw, 0, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private void putVertex(BakedQuadBuilder builder, Vector3d normal, double x, double y, double z, float u, float v, float r, float g, float b) {
        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
        for (int j = 0; j < elements.size(); j++) {
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
//                            float iu = sprite.getInterpolatedU(u);
//                            float iv = sprite.getInterpolatedV(v);
                            builder.put(j, u, v);
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

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}