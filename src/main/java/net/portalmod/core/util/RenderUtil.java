package net.portalmod.core.util;

import com.mojang.math.Axis;
import static org.lwjgl.opengl.GL11.GL_CLIP_PLANE0;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glClipPlane;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import com.mojang.blaze3d.systems.RenderSystem;

import net.portalmod.PMState;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.render.Shader;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.math.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.core.Vec3i;

public class RenderUtil {
    public static void bindTexture(Shader shader, String uniform, String path, int index) {
        // The mod's own shaders sample the texture bound to this GL unit (like 1.16 TextureManager#bind).
        RenderSystem.activeTexture(GL_TEXTURE0 + index);
        Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation(PortalMod.MODID, path)).bind();
        shader.setInt(uniform, index);
    }

    /**
     * TODO(port): 1.16 clipped the world behind a portal with fixed-function glClipPlane. Minecraft
     * 1.17+ runs a core-profile GL context where glClipPlane does not exist, so clipping has to move
     * into shaders (gl_ClipDistance). Until the portal renderer is redesigned this does nothing, and
     * geometry behind the portal surface may show through.
     */
    public static void setClipPlane(int index, Matrix4f matrix, Vec3 normal) {
    }

    public static void setStandardClipPlane(Matrix4f matrix) {
        setClipPlane(0, matrix, new Vec3(0, 0, -1));
    }

    public static void setupClipPlane(PoseStack clipMatrix, PortalEntity portal, Camera camera, float offset, boolean reversed) {
        Vec3i portalNormal = portal.getDirection().getNormal();
        Vec3 offsetNormal = new Vec3(camera.getPosition()).sub(portal.position()).normalize().mul(offset);
        net.minecraft.world.phys.Vec3 cameraPos = camera.getPosition();
        float roll = camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : PMState.cameraRoll;

        clipMatrix.last().pose().identity();
        clipMatrix.mulPose(Axis.ZP.rotationDegrees(roll));
        clipMatrix.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        clipMatrix.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180));
        clipMatrix.translate(offsetNormal.x, offsetNormal.y, offsetNormal.z);
        clipMatrix.translate(portalNormal.getX() * 0.0001f, portalNormal.getY() * 0.0001f, portalNormal.getZ() * 0.0001f);
        clipMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(clipMatrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());

        setClipPlane(0, clipMatrix.last().pose(), new Vec3(0, 0, reversed ? 1 : -1));
    }

    /** 1.16 Vector3f#rotationDegrees: a rotation of {@code degrees} around {@code axis}. */
    public static org.joml.Quaternionf rotationDegrees(org.joml.Vector3f axis, float degrees) {
        return new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(degrees), axis);
    }

    /**
     * Replacement for 1.16 TextureManager#bind: records the texture for vanilla shaders and also
     * binds it to the active GL unit for the mod's own shaders.
     */
    public static void bindTextureNow(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
        Minecraft.getInstance().getTextureManager().getTexture(texture).bind();
    }
}
