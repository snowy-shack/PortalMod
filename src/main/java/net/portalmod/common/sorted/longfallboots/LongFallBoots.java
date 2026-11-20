package net.portalmod.common.sorted.longfallboots;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.portalmod.PortalMod;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class LongFallBoots extends ArmorItem {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/models/armor/longfall_boots.png");
    
    public LongFallBoots(ArmorMaterial armorMaterial, EquipmentSlot slotType, Properties properties) {
        super(armorMaterial, slotType, properties);
    }
    
    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    public <A extends HumanoidModel<?>> A getArmorModel(LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, A originalModel) {
        return (A)(new LongFallBootsModel(entity));
    }
    
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return TEXTURE.toString();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("longfall_boots", list);
    }
}