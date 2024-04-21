package net.portalmod.core.util;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.portalmod.client.screens.PortalModOptionsScreen;

import java.util.List;

public class ModUtil {
    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction, int multiplier) {
        Vector3i normal = direction.getNormal();
        return shape.move(normal.getX() * multiplier, normal.getY() * multiplier, normal.getZ() * multiplier);
    }
    
    public static VoxelShape moveVoxelShape(VoxelShape shape, Direction direction) {
        return moveVoxelShape(shape, direction, 1);
    }
    
    public static BlockRayTraceResult rayTraceBlock(PlayerEntity player, World level, int length) {
        Vector3d rayPath = player.getViewVector(0).scale(length);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
        return level.clip(rayCtx);
    }

    public static Vector3d getOldPos(Entity entity) {
        return new Vector3d(entity.xo, entity.yo, entity.zo);
    }

    public static void addTooltip(String name, List<ITextComponent> list) {
        if (!PortalModOptionsScreen.TOOLTIPS.get()) {
            return;
        }

        if (!Screen.hasControlDown()) {
            list.add(new TranslationTextComponent("tooltip.portalmod.hold_control").withStyle(TextFormatting.GRAY));
            return;
        }

        // Single line
        if (I18n.exists("tooltip.portalmod." + name)) {
            list.add(new TranslationTextComponent("tooltip.portalmod." + name).withStyle(TextFormatting.GRAY));
            return;
        }

        // Multi line
        for (int i = 1; i < 100; i++) {
            String key = "tooltip.portalmod." + name + "_" + i;
            if (!I18n.exists(key)) {
                break;
            }
            list.add(new TranslationTextComponent(key).withStyle(TextFormatting.GRAY));
        }
    }
}