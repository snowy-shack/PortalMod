package net.portalmod.common.sorted.portalgun;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.core.init.BlockTagInit;
import net.portalmod.core.util.Colour;

public class PortalGunCrosshairRenderer {
//    public static final ResourceLocation CROSSHAIRS = new ResourceLocation(PortalMod.MODID, "textures/crosshairs.png");
    public static final ResourceLocation CROSSHAIRS =
            new ResourceLocation(PortalMod.MODID, "textures/gui/crosshair/crosshair_normal.png");
    public static final ResourceLocation CROSSHAIRS_P1 =
            new ResourceLocation(PortalMod.MODID, "textures/gui/crosshair/crosshair_p1.png");
    
    public static void render(MatrixStack matrixStack) {
        ItemStack itemStack = Minecraft.getInstance().player.getMainHandItem();

        int style = 2;

        if(itemStack.getItem() instanceof PortalGun
                && Minecraft.getInstance().options.getCameraType().isFirstPerson()
                && Minecraft.getInstance().gameMode.getPlayerMode() != GameType.SPECTATOR) {
            int u = 0;
            int v = 0;
            
//            if(!PortalGun.hasPortal(itemStack, PortalEnd.PRIMARY, true))
//                u = 33;
//            if(!PortalGun.hasPortal(itemStack, PortalEnd.SECONDARY, true))
//                v = 33;
            
//            BlockRayTraceResult rayTrace = ModUtil.rayTraceBlock(Minecraft.getInstance().player, Minecraft.getInstance().level, 100);

            PlayerEntity player = Minecraft.getInstance().player;
            World level = Minecraft.getInstance().level;

            Vector3d rayPath = player.getViewVector(0).scale(100);
            Vector3d from = player.getEyePosition(0);
            Vector3d to = from.add(rayPath);

            RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, null);
            BlockRayTraceResult rayTrace = level.clip(rayCtx);




            boolean b = BlockTagInit.isPortalable(Minecraft.getInstance().level.getBlockState(rayTrace.getBlockPos()).getBlock());
            
            // TODO only where placeable
            if(style == 1)
                u = b ? 33 : 0;

            if(style == 2)
//                u = PortalManager.has(PortalGun.getUUID(itemStack), PortalEnd.PRIMARY) ? 33 : 0;
                u = PortalManager.clientHas(PortalGun.getUUID(itemStack), PortalEnd.PRIMARY) ? 33 : 0;

//            if(style == 2)
//                u = PortalPairCache.CLIENT.has(PortalGun.getUUID(itemStack), PortalEnd.PRIMARY) ? 33 : 0;
            
//            u = PortalGun.hasPortal(itemStack, PortalEnd.PRIMARY, true) ? 33 : 0;
            v = 0;
            
//            Colour colour = Colour.fromHSV((int)System.currentTimeMillis() / 10 % 360, .8f, 1);
//            Colour oppositeColour = Colour.fromHSV((int)(System.currentTimeMillis() / 10 + 180) % 360, .8f, 1);
//            Colour colour = new Colour(Color.HSBtoRGB((int)(System.currentTimeMillis() / 10 % 360) / 360f, .8f, 1));
//            Colour oppositeColour = new Colour(Color.HSBtoRGB((int)((System.currentTimeMillis() / 10 + 180) % 360) / 360f, .8f, 1));

            CompoundNBT nbt = itemStack.getOrCreateTag();

            Colour leftColour = PortalGun.getLeftColour(nbt);
            Colour rightColour = PortalGun.getRightColour(nbt);

//            if(nbt.contains("portalHue")) {
//                float hue = (nbt.getInt("portalHue") % 360) / 360.0f;
//                colour = new Colour(Color.HSBtoRGB(hue, .8f, 1));
//                oppositeColour = new Colour(Color.HSBtoRGB(hue + .5f, .8f, 1));

//                int color = DyeColor.valueOf(nbt.getString("leftColor").toUpperCase()).getColorValue();
//                float[] hsv = new float[3];
//                Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
//                leftColour = new Colour(Color.HSBtoRGB(hsv[0], .8f, 1));
//                rightColour = new Colour(Color.HSBtoRGB(hsv[0] + .5f, .8f, 1));
//            }

            // blit(stack, x, y, z, u, v, uWidth, uHeight, texwidth, texheight)
            RenderSystem.disableBlend();
            Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS);
            blit(matrixStack,
                    Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17,
                    Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16,
                    0,
                    leftColour,
                    u, v, 33, 33, 66, 66);
            RenderSystem.enableBlend();
            
//            u = PortalGun.hasPortal(itemStack, PortalEnd.SECONDARY, true) ? 33 : 0;
            v = 33;


            if(style == 2)
//                u = PortalManager.has(PortalGun.getUUID(itemStack), PortalEnd.SECONDARY) ? 33 : 0;
                u = PortalManager.clientHas(PortalGun.getUUID(itemStack), PortalEnd.SECONDARY) ? 33 : 0;

//            if(style == 2)
//                u = PortalPairCache.CLIENT.has(PortalGun.getUUID(itemStack), PortalEnd.SECONDARY) ? 33 : 0;
            
            RenderSystem.disableBlend();
            Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS);
            blit(matrixStack,
                    Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17,
                    Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16,
                    0,
                    rightColour,
                    u, v, 33, 33, 66, 66);
            RenderSystem.enableBlend();
            
//            RenderSystem.disableBlend();
//            Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS_P1);
//            blit(matrixStack,
//                    Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17,
//                    Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16,
//                    0,
//                    new Colour(0xFFFFFF00),
//                    u, v, 33, 33, 66, 66);
//            RenderSystem.enableBlend();

            if(style == 1) {
                v = 0;
                u = 0;

//                if(PortalPairCache.CLIENT.has(PortalGun.getUUID(itemStack), PortalEnd.PRIMARY)) {
//                    // blit(stack, x, y, z, u, v, uWidth, uHeight, texwidth, texheight)
//                    RenderSystem.disableBlend();
//                    Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS_P1);
//                    blit(matrixStack,
//                            Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17,
//                            Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16,
//                            0,
//                            colour,
//                            u, v, 33, 33, 66, 33);
//                    RenderSystem.enableBlend();
//                }

//            u = PortalGun.hasPortal(itemStack, PortalEnd.SECONDARY, true) ? 33 : 0;
                u = 33;

//                if(PortalPairCache.CLIENT.has(PortalGun.getUUID(itemStack), PortalEnd.SECONDARY)) {
//                    RenderSystem.disableBlend();
//                    Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS_P1);
//                    blit(matrixStack,
//                            Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17,
//                            Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16,
//                            0,
//                            oppositeColour,
//                            u, v, 33, 33, 66, 33);
//                    RenderSystem.enableBlend();
//                }
            }
        }
    }
    
    public static void blit(MatrixStack matrixStack, int x, int y, int z, Colour colour, float u0, float v0, int uw, int uh, int width, int height) {
        innerBlit(matrixStack,
                x, x + uw,
                y, y + uh,
                z,
                colour,
                uw, uh, u0, v0,
                width, height);
    }
    
    private static void innerBlit(MatrixStack matrixStack, int x0, int x1, int y0, int y1, int z, Colour colour, int uw, int uh, float u0, float v0, int width, int height) {
        innerBlit(matrixStack.last().pose(),
                x0, x1, y0, y1, z,
                colour,
                (u0 + 0) / (float)width,
                (u0 + uw) / (float)width,
                (v0 + 0) / (float)height,
                (v0 + uh) / (float)height);
    }
    
    private static void innerBlit(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, Colour colour, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix, (float)x0, (float)y1, (float)z).color(colour.getIntR(), colour.getIntG(), colour.getIntB(), colour.getIntA()).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)z).color(colour.getIntR(), colour.getIntG(), colour.getIntB(), colour.getIntA()).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y0, (float)z).color(colour.getIntR(), colour.getIntG(), colour.getIntB(), colour.getIntA()).uv(u1, v0).endVertex();
        bufferbuilder.vertex(matrix, (float)x0, (float)y0, (float)z).color(colour.getIntR(), colour.getIntG(), colour.getIntB(), colour.getIntA()).uv(u0, v0).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end(bufferbuilder);
        
//        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//        bufferbuilder.vertex(matrix, (float)x0, (float)y1, (float)z).uv(u0, v1).endVertex();
//        bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)z).uv(u1, v1).endVertex();
//        bufferbuilder.vertex(matrix, (float)x1, (float)y0, (float)z).uv(u1, v0).endVertex();
//        bufferbuilder.vertex(matrix, (float)x0, (float)y0, (float)z).uv(u0, v0).endVertex();
//        bufferbuilder.end();
//        RenderSystem.enableAlphaTest();
//        WorldVertexBufferUploader.end(bufferbuilder);
    }
}