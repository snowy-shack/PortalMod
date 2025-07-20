package net.portalmod.core.util;

import static org.lwjgl.opengl.GL11.GL_CLIP_PLANE0;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glClipPlane;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.render.Shader;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.math.Vec3;

public class RenderUtil {
    public static void bindTexture(Shader shader, String uniform, String path, int index) {
        RenderSystem.activeTexture(GL_TEXTURE0 + index);
        Minecraft.getInstance().textureManager.bind(new ResourceLocation(PortalMod.MODID, path));
        shader.setInt(uniform, index);
    }

    public static void setClipPlane(int index, Matrix4f matrix, Vec3 normal) {
        glEnable(GL_CLIP_PLANE0 + index);
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(matrix);
        glClipPlane(GL_CLIP_PLANE0 + index, new double[] { normal.x, normal.y, normal.z, 0 });
        RenderSystem.popMatrix();
    }

    public static void setStandardClipPlane(Matrix4f matrix) {
        setClipPlane(0, matrix, new Vec3(0, 0, -1));
    }

    public static void setupClipPlane(MatrixStack clipMatrix, PortalEntity portal, ActiveRenderInfo camera, float offset, boolean reversed) {
        Vector3i portalNormal = portal.getDirection().getNormal();
        Vec3 offsetNormal = new Vec3(camera.getPosition()).sub(portal.position()).normalize().mul(offset);
        Vector3d cameraPos = camera.getPosition();

        clipMatrix.last().pose().setIdentity();
        if(camera instanceof PortalCamera)
            clipMatrix.mulPose(Vector3f.ZP.rotationDegrees(((PortalCamera)camera).getRoll()));
        clipMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        clipMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180));
        clipMatrix.translate(offsetNormal.x, offsetNormal.y, offsetNormal.z);
        clipMatrix.translate(portalNormal.getX() * 0.0001f, portalNormal.getY() * 0.0001f, portalNormal.getZ() * 0.0001f);
        clipMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(clipMatrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());

        setClipPlane(0, clipMatrix.last().pose(), new Vec3(0, 0, reversed ? 1 : -1));
    }
}