package net.portalmod.common.sorted.creer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;

public class CreerRenderer extends MobRenderer<Creeper, CreerModel<Creeper>> {
    public static final LazyLoadedValue<CreerRenderer> INSTANCE = new LazyLoadedValue<>(() ->
        new CreerRenderer(Minecraft.getInstance().getEntityRenderDispatcher()));
    private static final ResourceLocation CREEPER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper.png");

    private CreerRenderer(EntityRenderDispatcher p_i46186_1_) {
        super(p_i46186_1_, new CreerModel<>(), 0.5F);
        this.addLayer(new CreerChargeLayer(this));
    }

    protected void scale(Creeper p_225620_1_, PoseStack p_225620_2_, float p_225620_3_) {
        float f = p_225620_1_.getSwelling(p_225620_3_);
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f = f * f;
        f = f * f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        p_225620_2_.scale(f2, f3, f2);
    }

    protected float getWhiteOverlayProgress(Creeper p_225625_1_, float p_225625_2_) {
        float f = p_225625_1_.getSwelling(p_225625_2_);
        return (int)(f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
    }

    public ResourceLocation getTextureLocation(Creeper p_110775_1_) {
        return CREEPER_LOCATION;
    }
    
    public static boolean isCreer(Entity creeper) {
        return creeper instanceof Creeper
                && creeper.hasCustomName()
                && ChatFormatting.stripFormatting(creeper.getName().getString()).equalsIgnoreCase("creer");
    }
}