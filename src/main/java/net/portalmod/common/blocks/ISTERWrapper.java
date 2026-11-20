package net.portalmod.common.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;

public class ISTERWrapper extends BlockEntityWithoutLevelRenderer {
    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = ((BakedModelWrapper)itemRenderer.getModel(itemStack, Minecraft.getInstance().level, Minecraft.getInstance().player)).getBase();

        matrixStack.popPose();
        matrixStack.pushPose();
        model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStack, model, transformType, transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
        matrixStack.translate(-0.5D, -0.5D, -0.5D);

        RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, true);
        VertexConsumer vertexBuilder = itemRenderer.getFoilBufferDirect(renderTypeBuffer, renderType, true, itemStack.hasFoil());
        itemRenderer.renderModelLists(model, itemStack, light, overlay, matrixStack, vertexBuilder);
    }
}