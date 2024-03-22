package net.portalmod.core.util;

import static org.lwjgl.opengl.GL11.GL_CLIP_PLANE0;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glClipPlane;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.PortalMod;
import net.portalmod.client.render.Shader;

public class RenderUtil {
    public static void bindTexture(Shader shader, String uniform, String path, int index) {
        RenderSystem.activeTexture(GL_TEXTURE0 + index);
        Minecraft.getInstance().textureManager.bind(new ResourceLocation(PortalMod.MODID, path));
        shader.setInt(uniform, index);
    }
    
    public static void setClipPlane(int index, ActiveRenderInfo camera, Vector3d pos, Vector3d normal) {
        Vector3d cameraPos = camera.getPosition();
        
        MatrixStack clipMatrix = new MatrixStack();
        clipMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        clipMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        clipMatrix.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
        
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.multMatrix(clipMatrix.last().pose());
        enableClipPlane(index);
        glClipPlane(GL_CLIP_PLANE0 + index, new double[] {normal.x, normal.y, normal.z, 0});
        RenderSystem.popMatrix();
    }
    
    public static void enableClipPlane(int index) {
        glEnable(GL_CLIP_PLANE0 + index);
    }
    
    public static void disableClipPlane(int index) {
        glDisable(GL_CLIP_PLANE0 + index);
    }
}