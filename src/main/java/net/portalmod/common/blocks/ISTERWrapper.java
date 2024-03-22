package net.portalmod.common.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class ISTERWrapper extends ItemStackTileEntityRenderer {
    @Override
    public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        IBakedModel model = ((BakedModelWrapper)itemRenderer.getModel(itemStack, Minecraft.getInstance().level, Minecraft.getInstance().player)).getBase();

        matrixStack.popPose();
        matrixStack.pushPose();
        model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
        matrixStack.translate(-0.5D, -0.5D, -0.5D);

        RenderType renderType = RenderTypeLookup.getRenderType(itemStack, true);
        IVertexBuilder vertexBuilder = itemRenderer.getFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
        itemRenderer.renderModelLists(model, itemStack, light, overlay, matrixStack, vertexBuilder);
    }
}