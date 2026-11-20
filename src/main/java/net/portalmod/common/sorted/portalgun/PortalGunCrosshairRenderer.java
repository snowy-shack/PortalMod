package net.portalmod.common.sorted.portalgun;

import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.PortalMod;
import net.portalmod.common.blocks.PortalableBlock;
import net.portalmod.common.sorted.portal.ClientPortalManager;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.config.PortalModConfigManager;

import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class PortalGunCrosshairRenderer {
    private static final String BASE = "textures/gui/crosshair/portalgun_crosshair_";
    private static final int CROSSHAIR_SIZE = 33;

    public static void render(PoseStack matrixStack) {
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
            Player player = mc.player;
            Level level = mc.level;

            Vec3 rayPath = player.getViewVector(0).scale(mc.gameRenderer.getRenderDistance());
            Vec3 from = player.getEyePosition(0);
            Vec3 to = from.add(rayPath);

            ClipContext rayCtx = new ClipContext(from, to, ClipContext.BlockMode.COLLIDER, ClipContext.FluidMode.ANY, null);
            BlockHitResult rayTrace = PortalGun.customClip(level, rayCtx);

            boolean isPortalable = PortalableBlock.isPortalable(mc.level.getBlockState(rayTrace.getBlockPos()), rayTrace.getDirection(), level);
            primaryFilled = isPortalable;
            secondaryFilled = isPortalable;
            // todo actually figure out whether the portal is placeable there
        } else {
            primaryFilled = ClientPortalManager.getInstance().hasFullOrPartial(uuid.get(), PortalEnd.PRIMARY);
            secondaryFilled = ClientPortalManager.getInstance().hasFullOrPartial(uuid.get(), PortalEnd.SECONDARY);

            CompoundTag tag = itemStack.getTag();
            if(tag != null && (tag.contains("Locked") && !tag.getString("Locked").equals("None"))) {
                if(tag.getString("Locked").equals("Right")) {
                    secondaryFilled = primaryFilled;
                }

                if(tag.getString("Locked").equals("Left")) {
                    primaryFilled = secondaryFilled;
                }
            }
        }

        CompoundTag nbt = itemStack.getOrCreateTag();
        DyeColor primaryColor = PortalGun.getLeftDyeColour(nbt);
        DyeColor secondaryColor = PortalGun.getRightDyeColour(nbt);
        renderCrosshairPart(matrixStack, PortalEnd.PRIMARY, primaryColor, primaryFilled);
        renderCrosshairPart(matrixStack, PortalEnd.SECONDARY, secondaryColor, secondaryFilled);

        if(isClassicCrosshair) {
            CompoundTag tag = itemStack.getTag();
            if(tag != null) {
                if(tag.contains("LastPortal") && (!tag.contains("Locked") || tag.getString("Locked").equals("None"))) {
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

    private static void renderCrosshairPart(PoseStack matrixStack, PortalEnd end, DyeColor color, boolean filled) {
        ResourceLocation texture = new ResourceLocation(PortalMod.MODID, BASE + color.getName() + ".png");
        Window window = Minecraft.getInstance().getWindow();
        int x = centeredCrosshairOrigin(window.getGuiScaledWidth(), CROSSHAIR_SIZE);
        int y = centeredCrosshairOrigin(window.getGuiScaledHeight(), CROSSHAIR_SIZE);

        RenderSystem.disableBlend();
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(matrixStack, x, y, 0,
                filled ? CROSSHAIR_SIZE : 0, end == PortalEnd.PRIMARY ? 0 : CROSSHAIR_SIZE, CROSSHAIR_SIZE, CROSSHAIR_SIZE, CROSSHAIR_SIZE * 2, CROSSHAIR_SIZE * 2);
        RenderSystem.enableBlend();
    }

    private static void renderCrosshairDot(PoseStack matrixStack, PortalEnd end, DyeColor color) {
        ResourceLocation texture = new ResourceLocation(PortalMod.MODID, BASE + "dots_" + color.getName() + ".png");
        Window window = Minecraft.getInstance().getWindow();
        int x = centeredCrosshairOrigin(window.getGuiScaledWidth(), CROSSHAIR_SIZE);
        int y = centeredCrosshairOrigin(window.getGuiScaledHeight(), CROSSHAIR_SIZE);

        RenderSystem.disableBlend();
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(matrixStack, x, y, 0,
                0, end == PortalEnd.PRIMARY ? 0 : CROSSHAIR_SIZE, CROSSHAIR_SIZE, CROSSHAIR_SIZE, CROSSHAIR_SIZE, CROSSHAIR_SIZE * 2);
        RenderSystem.enableBlend();
    }

    static int centeredCrosshairOrigin(int guiSize, int spriteSize) {
        return (guiSize - spriteSize) / 2;
    }

    private static void blit(PoseStack matrixStack, int x, int y, int z, float u0, float v0, int uw, int uh, int width, int height) {
        innerBlit(matrixStack,
                x, x + uw,
                y, y + uh,
                z,
                uw, uh, u0, v0,
                width, height);
    }
    
    private static void innerBlit(PoseStack matrixStack, int x0, int x1, int y0, int y1, int z, int uw, int uh, float u0, float v0, int width, int height) {
        innerBlit(matrixStack.last().pose(),
                x0, x1, y0, y1, z,
                (u0 + 0) / (float)width,
                (u0 + uw) / (float)width,
                (v0 + 0) / (float)height,
                (v0 + uh) / (float)height);
    }
    
    private static void innerBlit(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix, (float)x0, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v1).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u1, v0).endVertex();
        bufferbuilder.vertex(matrix, (float)x0, (float)y0, (float)z).color(1f, 1f, 1f, 1f).uv(u0, v0).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        BufferUploader.end(bufferbuilder);
    }
}
