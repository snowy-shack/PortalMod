package net.portalmod.common.sorted.portalgun;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.PortalMod;
import net.portalmod.client.animation.AnimatedTexture;
import net.portalmod.core.init.AnimationInit;
import net.portalmod.core.util.Colour;

import java.util.UUID;

public class PortalGunISTER extends ItemStackTileEntityRenderer {
    public static final ResourceLocation PORTALGUN_TEXTURE = new ResourceLocation(PortalMod.MODID, "gun/portalgun");
    public static final ResourceLocation PORTALGUN_TEXTURE2 = new ResourceLocation(PortalMod.MODID, "gun/portalgun_color");
    public static final ResourceLocation MISSINGNO_TEXTURE = new ResourceLocation("missingno");
    public static RenderMaterial PORTALGUN_MATERIAL;
    public static RenderMaterial PORTALGUN_MATERIAL2;
    public static RenderMaterial MISSINGNO_MATERIAL;
    public static PortalGunModel PORTALGUN_MODEL;
    public final AnimatedTexture TEX = new AnimatedTexture(AtlasTexture.LOCATION_BLOCKS,
            new ResourceLocation(PortalMod.MODID, "gun/portalgun"), 1 /*3*/, 1); //FIXME
    
    public PortalGunISTER() {
        PORTALGUN_MODEL = new PortalGunModel();
        PORTALGUN_MATERIAL = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, PORTALGUN_TEXTURE);
        PORTALGUN_MATERIAL2 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, PORTALGUN_TEXTURE2);
        MISSINGNO_MATERIAL = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, MISSINGNO_TEXTURE);
    }
    
    @Override
    public void renderByItem(ItemStack itemStack, TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int packedLight, int packedOverlay) {
        matrixStack.pushPose();
        
        switch(transformType) {
        case FIRST_PERSON_LEFT_HAND:
            matrixStack.translate(.2, .1, 0);
            matrixStack.scale(1.5f, 1.5f, 1.5f);
            break;
            
        case FIRST_PERSON_RIGHT_HAND:
            matrixStack.translate(.8, .1, 0);
            matrixStack.scale(1.5f, 1.5f, 1.5f);
            break;
            
        case GROUND:
            matrixStack.translate(.5, .35, .5);
            matrixStack.scale(.75f, .75f, .75f);
            break;
            
        case GUI:
            matrixStack.translate(.55, .3, 0);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(30));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-135));
            break;
            
        case HEAD:
            matrixStack.translate(.5, .5, 0);
            matrixStack.scale(1.5f, 1.5f, 1.5f);
            break;
            
        case THIRD_PERSON_LEFT_HAND:
            matrixStack.translate(.5, .2, .1);
            break;
            
        case THIRD_PERSON_RIGHT_HAND:
            matrixStack.translate(.5, .2, .1);
            break;
            
        case FIXED:
            matrixStack.translate(.5, .3, .45);
//            matrixStack.mulPose(Vector3f.XP.rotationDegrees(30));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
            matrixStack.scale(1.3f, 1.3f, 1.3f);
            break;
            
        default:
            break;
        }
        
        boolean animate = true;
        
        if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
//            if (InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_G)) {
//                PORTALGUN_MODEL.startAnimation(itemStack, "shoot");
//                AnimationInit.RECOIL_X.start();
//                AnimationInit.RECOIL_Y.start();
//            }
            
//            if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_H)) {
//                AnimationInit.FIZZLE_BODY.start();
//            }
            
            float xRot = (float) ((float) AnimationInit.RECOIL_X.compute(System.currentTimeMillis())
                    + 1.5 * AnimationInit.LIFT.compute(System.currentTimeMillis()));
            float yRot = (float) ((float) AnimationInit.RECOIL_Y.compute(System.currentTimeMillis())
                    + AnimationInit.LIFT.compute(System.currentTimeMillis()));
            float zRot = (float) ((float) AnimationInit.FIZZLE_BODY.compute(System.currentTimeMillis())
                    + .5 * AnimationInit.LIFT.compute(System.currentTimeMillis()));
            
            matrixStack.translate(0, 0, .5);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
            matrixStack.translate(0, 0, -.5);
        } else {
            animate = false;
        }
        
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrixStack.translate(0, -1.5, 0);
        
//        Colour colour = new Colour(Color.HSBtoRGB((int)(System.currentTimeMillis() / 10 % 360) / 360f, .8f, 1));
        
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();
        irendertypebuffer$impl.endBatch();
        IVertexBuilder ivertexbuilder = TEX.buffer(renderTypeBuffer, RenderType::entityCutoutNoCull);
        
//        ResourceLocation gun = new ResourceLocation(PortalMod.MODID, "gun/portalgun_nitro_anim");

        UUID gunUUID = PortalGun.getUUID(itemStack);

        Colour colour = new Colour(255, 0, 0, 255);
        Colour oppositeColour = new Colour(255, 0, 0, 255);
        Colour lastPortalColor = new Colour(64, 59, 75, 255);
        Colour stripeColour = new Colour(255, 255, 255, 0);

        boolean gunLightOn = false;

        CompoundNBT nbt = itemStack.getOrCreateTag();
//        if(nbt.contains("portalHue")) {
//            int color = DyeColor.valueOf(nbt.getString("portalHue").toUpperCase()).getColorValue();
//            float[] hsv = new float[3];
//            Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
//
//            colour = new Colour(Color.HSBtoRGB(hsv[0], .8f, 1));
//            oppositeColour = new Colour(Color.HSBtoRGB(hsv[0] + .5f, .8f, 1));
//        }
//        if(nbt.contains("color")) {
//            lastPortalColor = nbt.getByte("color") == 1 ? colour : oppositeColour;
//        }
//        if(nbt.contains("gunHue")) {
//            int color = DyeColor.valueOf(nbt.getString("gunHue").toUpperCase()).getColorValue();
//            float[] hsv = new float[3];
//            Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
//        }
        if(nbt.contains("AccentColor")) {
            if (nbt.getString("AccentColor").equals("none")) {
                stripeColour = new Colour(255, 255, 255, 0);
            } else stripeColour = new Colour(DyeColor.byName(nbt.getString("AccentColor"), DyeColor.RED).getTextureDiffuseColors());
        }
        if(nbt.contains("LastPortal")) {
            int lastPortal = nbt.getInt("LastPortal");
            if (lastPortal != 0) gunLightOn = true;
            switch (lastPortal) {
                case -1 : lastPortalColor = PortalGun.getLeftColour(nbt);
                break;
                case 1 : lastPortalColor = PortalGun.getRightColour(nbt);
            }
            lastPortalColor.lighten(0.05f + 0.1f * (float) Math.sin((System.currentTimeMillis() / 10.0 % 360) * Math.PI / 180));
        }

        TEX.setupAnimation();
        PORTALGUN_MODEL.render(gunUUID, PORTALGUN_MODEL.gun, matrixStack, ivertexbuilder, packedLight, packedOverlay, new Colour(255, 255, 255, 255), !animate);
        PORTALGUN_MODEL.render(gunUUID, PORTALGUN_MODEL.stripes, matrixStack, ivertexbuilder, packedLight, packedOverlay, stripeColour, !animate);
        irendertypebuffer$impl.endBatch();
        TEX.endAnimation();

        ivertexbuilder = PORTALGUN_MATERIAL2.buffer(renderTypeBuffer, RenderType::entityTranslucent);
        PORTALGUN_MODEL.render(gunUUID, PORTALGUN_MODEL.colour, matrixStack, ivertexbuilder, gunLightOn ? LightTexture.pack(15, 15) : packedLight, packedOverlay, lastPortalColor, !animate);

//        ivertexbuilder = PORTALGUN_MATERIAL.buffer(renderTypeBuffer, RenderType::entityTranslucent);
//        PORTALGUN_MODEL.render(gunUUID, PORTALGUN_MODEL.stripes, matrixStack, ivertexbuilder, packedLight, packedOverlay, currentColour, !animate);
        
        matrixStack.popPose();
    }

    public static void startShootAnimation(UUID gunUUID) {
        PORTALGUN_MODEL.startAnimation(gunUUID, "shoot");
        AnimationInit.RECOIL_X.start();
        AnimationInit.RECOIL_Y.start();
    }

    public static void startFizzleAnimation() {
        AnimationInit.FIZZLE_BODY.start();
    }

    public static void startLiftAnimation(UUID gunUUID) {
        PORTALGUN_MODEL.startAnimation(gunUUID, "lift");
        AnimationInit.LIFT.start();
    }

    public static void stopLiftAnimation(UUID gunUUID) {
        PORTALGUN_MODEL.startAnimation(gunUUID, "drop");
        AnimationInit.LIFT.stop();
    }
}