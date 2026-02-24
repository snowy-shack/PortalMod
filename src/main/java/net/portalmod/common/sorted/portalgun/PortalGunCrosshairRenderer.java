package net.portalmod.common.sorted.portalgun;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
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
import net.portalmod.common.sorted.portal.ClientPortalManager;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.BlockTagInit;

import java.util.Optional;
import java.util.UUID;

public class PortalGunCrosshairRenderer {
    private static final String BASE = "textures/gui/crosshair/portalgun_crosshair_";

    public static void render(MatrixStack matrixStack) {
        Minecraft mc = Minecraft.getInstance();

        if(mc.level == null || mc.player == null || mc.gameMode == null)
            return;

        ItemStack itemStack = mc.player.getMainHandItem();
        Optional<UUID> uuid = PortalGun.getUUID(itemStack);
        boolean isClassicCrosshair = PortalModConfigManager.CROSSHAIR.get();
        boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
        boolean isSpectator = mc.gameMode.getPlayerMode() == GameType.SPECTATOR;

        if(!(itemStack.getItem() instanceof PortalGun) || !isFirstPerson || isSpectator || !uuid.isPresent())
            return;

        boolean primaryFilled;
        boolean secondaryFilled;

        if(isClassicCrosshair) {
            PlayerEntity player = mc.player;
            World level = mc.level;

            Vector3d rayPath = player.getViewVector(0).scale(mc.gameRenderer.getRenderDistance());
            Vector3d from = player.getEyePosition(0);
            Vector3d to = from.add(rayPath);

            RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, null);
            BlockRayTraceResult rayTrace = level.clip(rayCtx);

            boolean isPortalable = BlockTagInit.isPortalable(mc.level.getBlockState(rayTrace.getBlockPos()).getBlock());
            primaryFilled = isPortalable;
            secondaryFilled = isPortalable;
            // todo actually figure out whether the portal is placeable there
        } else {
            primaryFilled = ClientPortalManager.getInstance().hasFullOrPartial(uuid.get(), PortalEnd.PRIMARY);
            secondaryFilled = ClientPortalManager.getInstance().hasFullOrPartial(uuid.get(), PortalEnd.SECONDARY);
        }

        CompoundNBT nbt = itemStack.getOrCreateTag();
        DyeColor primaryColor = PortalGun.getLeftDyeColour(nbt);
        DyeColor secondaryColor = PortalGun.getRightDyeColour(nbt);
        renderCrosshairPart(matrixStack, PortalEnd.PRIMARY, primaryColor, primaryFilled);
        renderCrosshairPart(matrixStack, PortalEnd.SECONDARY, secondaryColor, secondaryFilled);

        if(isClassicCrosshair) {
            CompoundNBT tag = itemStack.getTag();
            if(tag != null) {
                if(tag.contains("LastPortal")) {
                    int lastPortal = tag.getInt("LastPortal");
                    if(lastPortal == -1) {
                        renderCrosshairDot(matrixStack, PortalEnd.PRIMARY, primaryColor);
                    }
                    if(lastPortal == 1) {
                        renderCrosshairDot(matrixStack, PortalEnd.SECONDARY, secondaryColor);
                    }
                }
            }
        }
    }

    private static void renderCrosshairPart(MatrixStack matrixStack, PortalEnd end, DyeColor color, boolean filled) {
        ResourceLocation texture = new ResourceLocation(PortalMod.MODID, BASE + color.getName() + ".png");
        MainWindow window = Minecraft.getInstance().getWindow();
        int x = -17;
        int y = -16;
        int size = 33;

        RenderSystem.disableBlend();
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(matrixStack, window.getGuiScaledWidth() / 2 + x, window.getGuiScaledHeight() / 2 + y, 0,
                filled ? size : 0, end == PortalEnd.PRIMARY ? 0 : size, size, size, size * 2, size * 2);
        RenderSystem.enableBlend();
    }

    private static void renderCrosshairDot(MatrixStack matrixStack, PortalEnd end, DyeColor color) {
        ResourceLocation texture = new ResourceLocation(PortalMod.MODID, BASE + "dots_" + color.getName() + ".png");
        MainWindow window = Minecraft.getInstance().getWindow();
        int x = -17;
        int y = -16;
        int size = 33;

        RenderSystem.disableBlend();
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(matrixStack, window.getGuiScaledWidth() / 2 + x, window.getGuiScaledHeight() / 2 + y, 0,
                0, end == PortalEnd.PRIMARY ? 0 : size, size, size, size, size * 2);
        RenderSystem.enableBlend();
    }

    private static void blit(MatrixStack matrixStack, int x, int y, int z, float u0, float v0, int uw, int uh, int width, int height) {
        innerBlit(matrixStack,
                x, x + uw,
                y, y + uh,
                z,
                uw, uh, u0, v0,
                width, height);
    }
    
    private static void innerBlit(MatrixStack matrixStack, int x0, int x1, int y0, int y1, int z, int uw, int uh, float u0, float v0, int width, int height) {
        innerBlit(matrixStack.last().pose(),
                x0, x1, y0, y1, z,
                (u0 + 0) / (float)width,
                (u0 + uw) / (float)width,
                (v0 + 0) / (float)height,
                (v0 + uh) / (float)height);
    }
    
    private static void innerBlit(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix, (float)x0, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v0).endVertex();
        bufferbuilder.vertex(matrix, (float)x0, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v0).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end(bufferbuilder);
    }
}