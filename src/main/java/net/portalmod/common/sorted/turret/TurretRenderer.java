package net.portalmod.common.sorted.turret;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.PortalMod;
import net.portalmod.common.entity.TestElementEntityRenderer;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

public class TurretRenderer extends TestElementEntityRenderer<TurretEntity, TurretModel<TurretEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/turret/turret.png");
    private static final VertexBuffer LASER_BUFFER = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR);

    public TurretRenderer(EntityRendererManager erm) {
        super(erm, new TurretModel<>(), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TurretEntity turret) {
        return TEXTURE;
    }

    @Override
    public void render(TurretEntity turret, float a, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light) {
//        if(super.shouldRender(turret, ))

        // todo dont render turret if clipped
        super.render(turret, a, partialTicks, matrixStack, renderTypeBuffer, light);

        if (turret.isFizzling()) {
            return;
        }

        final float eyeHeight = 12f/16f;
        float rotation = -MathHelper.lerp(partialTicks, turret.yBodyRotO, turret.yBodyRot) * ((float)Math.PI / 180f);
        float z = MathHelper.cos(rotation);
        float x = MathHelper.sin(rotation);

        Vec3 turretEyePos = new Vec3(turret.getPosition(partialTicks).add(0, eyeHeight, 0));
        Vec3 turretEyeToCamera = new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())
                .sub(turretEyePos)
                .normalize();

        Vec3 laserForward = new Vec3(x, 0, z);
        Vec3 projectedTurretEyeToCamera = turretEyeToCamera.sub(laserForward.clone().mul(turretEyeToCamera.dot(laserForward)));
        Vec3 laserUp = projectedTurretEyeToCamera.clone().normalize();
        Vec3 laserRight = laserUp.clone().cross(laserForward).normalize();

        Mat4 laserMatrix = new Mat4(
                laserRight.x, laserUp.x, laserForward.x, 0,
                laserRight.y, laserUp.y, laserForward.y, 0,
                laserRight.z, laserUp.z, laserForward.z, 0,
                           0,         0,               0, 1
        );

        Vector3d rayPath = new Vector3d(x, 0, z).scale(Minecraft.getInstance().gameRenderer.getRenderDistance() * 2);
        Vector3d from = turretEyePos.to3d();
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, null);
        RayTraceResult rayResult = turret.level.clip(rayCtx);

        float laserLen = (float)rayResult.getLocation().subtract(from).length();
        float opacity = 1f - ((float)Math.sin((double)System.currentTimeMillis() / 1000d) + 1f) / 2f * 0.3f;

        matrixStack.pushPose();
        matrixStack.translate(0, eyeHeight, 0);
        matrixStack.last().pose().multiply(laserMatrix.to4f());

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        bufferBuilder.vertex( 1/32f, 0, 0).uv(0, 0).color(1, 1, 1, opacity).endVertex();
        bufferBuilder.vertex(-1/32f, 0, 0).uv(0, 1).color(1, 1, 1, opacity).endVertex();
        bufferBuilder.vertex(-1/32f, 0, laserLen).uv(laserLen / 4f, 1).color(1, 1, 1, opacity).endVertex();
        bufferBuilder.vertex( 1/32f, 0, laserLen).uv(laserLen / 4f, 0).color(1, 1, 1, opacity).endVertex();

        bufferBuilder.end();
        LASER_BUFFER.upload(bufferBuilder);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        Minecraft.getInstance().textureManager.bind(new ResourceLocation(PortalMod.MODID, "textures/entity/turret/laser.png"));
        LASER_BUFFER.bind();
        DefaultVertexFormats.POSITION_TEX_COLOR.setupBufferState(0L);
        LASER_BUFFER.draw(matrixStack.last().pose(), 7);
        VertexBuffer.unbind();
        DefaultVertexFormats.POSITION_TEX_COLOR.clearBufferState();

        matrixStack.popPose();
    }

    @Override
    public boolean shouldRender(TurretEntity p_225626_1_, ClippingHelper p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
        return true;
    }
}