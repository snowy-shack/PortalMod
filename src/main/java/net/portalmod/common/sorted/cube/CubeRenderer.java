package net.portalmod.common.sorted.cube;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.portalmod.common.entity.TestElementEntityRenderer;

public abstract class CubeRenderer extends TestElementEntityRenderer<Cube, CubeModel<Cube>> {
    public CubeRenderer(EntityRendererManager erm) {
        super(erm, new CubeModel<>(), 0.5f);
    }
    
//    private static final float[] projectionBuffer = new float[16];
    
    @Override
    public void render(Cube cube, float f, float f2, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i) {
//        glGetFloatv(GL_PROJECTION_MATRIX, projectionBuffer);
//        
//        Minecraft minecraft = Minecraft.getInstance();
//        boolean flag = this.isBodyVisible(cube);
//        boolean flag1 = !flag && !cube.isInvisibleTo(minecraft.player);
//        boolean flag2 = minecraft.shouldEntityAppearGlowing(cube);
//        RenderType renderType = this.getRenderType(cube, flag, flag1, flag2);
//        
//        AbstractCubeModel.shader.bind();
//        PortalShaders.uniformMatrix("projection", projectionBuffer);
//        RenderSystem.activeTexture(GL_TEXTURE0);
//        minecraft.textureManager.bind(minecraft.getEntityRenderDispatcher().getRenderer(cube).getTextureLocation(cube));
//        PortalShaders.uniform1i("texture", 0);
        
        super.render(cube, f, f2, matrixStack, renderTypeBuffer, i);

//        IRenderTypeBuffer.Impl irendertypebuffer$impl = minecraft.levelRenderer.renderBuffers.bufferSource();
//        irendertypebuffer$impl.endBatch(renderType);
//        
//        AbstractCubeModel.shader.unbind();
    }
}