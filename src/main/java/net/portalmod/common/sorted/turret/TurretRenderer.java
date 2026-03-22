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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.PortalMod;
import net.portalmod.common.entities.TestElementEntityRenderer;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;

public class TurretRenderer extends TestElementEntityRenderer<TurretEntity, TurretModel<TurretEntity>> {
//    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/entity/turret/turret.png");
    private static final ResourceLocation[] TEXTURE = new ResourceLocation[] {
            new ResourceLocation(PortalMod.MODID, "textures/entity/turret/turret.png"),
            new ResourceLocation(PortalMod.MODID, "textures/entity/turret/turret_off.png"),
    };
    private static final VertexBuffer LASER_BUFFER = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR);
    public static TurretState state = TurretState.RESTING;

    public TurretRenderer(EntityRendererManager erm) {
        super(erm, new TurretModel<>(), 0.5f);
        this.addLayer(new TurretEyeLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TurretEntity turret) {
        return TEXTURE[(turret.getState() == TurretState.DEAD) ? 1 : 0];
    }

    @Override
    public void render(TurretEntity turret, float a, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light) {
//        if(super.shouldRender(turret, ))
        TurretState turretState = turret.getState();

        float fallAnimTick = Math.min(turret.getAnimationTicks() + partialTicks, turret.fallDuration);
        float fallAmount = 90F * (fallAnimTick * fallAnimTick) / (turret.fallDuration * turret.fallDuration);
        int tipDir = turret.getTipDirectionRight() ? 1 : -1;
        fallAmount *= tipDir;
        float tipOffset = 0.2F;
        float halfModelHeight = 0.8F;

        if (turretState == TurretState.DEAD) {
            fallAmount = 90F * tipDir;
        }

        if (turretState == TurretState.FALLING || turretState == TurretState.DEAD) {
            Vector3f lookAngle = new Vector3f(turret.getLookAngle().multiply(1, 0, 1).normalize());

            matrixStack.pushPose();
            float progress = (turretState == TurretState.FALLING ? fallAnimTick / turret.fallDuration : 1);
            matrixStack.translate(
                    lookAngle.z() * halfModelHeight * tipDir * progress,
                    0,
                    -lookAngle.x() * halfModelHeight * tipDir * progress
            );

            matrixStack.translate(-lookAngle.z() * tipOffset * tipDir, 0, lookAngle.x() * tipOffset * tipDir);
            matrixStack.mulPose(lookAngle.rotationDegrees(fallAmount));
            matrixStack.translate(lookAngle.z() * tipOffset * tipDir, 0, -lookAngle.x() * tipOffset * tipDir);
        }

        // todo dont render turret if clipped
        super.render(turret, a, partialTicks, matrixStack, renderTypeBuffer, light);

        if (turretState == TurretState.FALLING || turretState == TurretState.DEAD) {
            matrixStack.popPose();
        }

        if (turret.isFizzling()) {
            return;
        }

        float rotation = -MathHelper.lerp(partialTicks, turret.yBodyRotO, turret.yBodyRot) * ((float)Math.PI / 180f);
        float z = MathHelper.cos(rotation);
        float x = MathHelper.sin(rotation);

        // Eye position at eye height and 2.5 pixels to the direction it is looking in
        Vector3d localEyePos = new Vector3d(x * 2.5 / 16, turret.getEyeHeight(), z * 2.5 / 16);
        Vec3 turretEyePos = new Vec3(turret.getPosition(partialTicks).add(localEyePos));
        Vec3 turretEyeToCamera = new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition())
                .sub(turretEyePos)
                .normalize();

        // The position that the laser should ease towards
        Vector3d targetPos;
        if (turret.hasTarget() && turret.shouldLaserMove()) {
            // Position at the target
            targetPos = turret.targetEntity.getPosition(partialTicks).add(0, turret.targetEntity.getBbHeight() * 0.5, 0);
        } else {
            // Position 5 blocks in front of the turret, when not looking at anything
            targetPos = turretEyePos.to3d().add(new Vector3d(x, 0, z).scale(5));
        }

        if (turret.lastLaserPos == Vector3d.ZERO) {
            turret.lastLaserPos = targetPos;
        }

        // Exponential smoothing - https://lisyarus.github.io/blog/posts/exponential-smoothing.html
        if (turret.shouldLaserEase()) {
            double deltaTime = Minecraft.getInstance().getDeltaFrameTime();
            targetPos = turret.lastLaserPos.add(targetPos.subtract(turret.lastLaserPos).scale(1 - Math.exp(- 0.5 * deltaTime)));
        }

        Vector3d turretToTarget = targetPos.subtract(turretEyePos.to3d());
        turret.turretToTarget = turretToTarget;

        Vec3 laserForward = new Vec3(turretToTarget).normalize();
        Vec3 projectedTurretEyeToCamera = turretEyeToCamera.sub(laserForward.clone().mul(turretEyeToCamera.dot(laserForward)));
        Vec3 laserUp = projectedTurretEyeToCamera.clone().normalize();
        Vec3 laserRight = laserUp.clone().cross(laserForward).normalize();

        Mat4 laserMatrix = new Mat4(
                laserRight.x, laserUp.x, laserForward.x,0,
                laserRight.y, laserUp.y, laserForward.y,0,
                laserRight.z, laserUp.z, laserForward.z,0,
                0,0, 0, 1
        );

        Vector3d rayPath = turretToTarget.scale(Minecraft.getInstance().gameRenderer.getRenderDistance() * 2);
        Vector3d from = turretEyePos.to3d();
        Vector3d to = from.add(rayPath);

//        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);
//        RayTraceResult rayResult = turret.level.clip(rayCtx);

//        float laserLen = (float)rayResult.getLocation().subtract(from).length();
        float laserLen = (float) turret.traceAsFarAsPossible(from, to).getSecond().subtract(from).length();
        float opacity = 1f - ((float)Math.sin((double)System.currentTimeMillis() / 1000d) + 1f) / 2f * 0.3f;

        matrixStack.pushPose();
        matrixStack.translate(localEyePos.x, localEyePos.y, localEyePos.z);
        matrixStack.last().pose().multiply(laserMatrix.to4f());

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        bufferBuilder.vertex( 1/32f, 0, 0).uv(0, 0).color(1, 1, 1, opacity).endVertex();
        bufferBuilder.vertex(-1/32f, 0, 0).uv(0, 1).color(1, 1, 1, opacity).endVertex();
        bufferBuilder.vertex(-1/32f, 0, laserLen).uv(laserLen / 4f, 1).color(1, 1, 1, opacity).endVertex();
        bufferBuilder.vertex( 1/32f, 0, laserLen).uv(laserLen / 4f, 0).color(1, 1, 1, opacity).endVertex();

        bufferBuilder.end();
        LASER_BUFFER.upload(bufferBuilder);

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
//        RenderSystem.disableCull(); // Causes transparent texture issues

        Minecraft.getInstance().textureManager.bind(new ResourceLocation(PortalMod.MODID, "textures/entity/turret/laser.png"));
        LASER_BUFFER.bind();
        DefaultVertexFormats.POSITION_TEX_COLOR.setupBufferState(0L);
        if (turretState != TurretState.FALLING && turretState != TurretState.DEAD && turret.getHurtTime() == 0) LASER_BUFFER.draw(matrixStack.last().pose(), 7);
        VertexBuffer.unbind();
        DefaultVertexFormats.POSITION_TEX_COLOR.clearBufferState();

        matrixStack.popPose();

        turret.lastLaserPos = targetPos;
    }

    @Override
    public boolean shouldRender(TurretEntity p_225626_1_, ClippingHelper p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
        return true;
    }
}