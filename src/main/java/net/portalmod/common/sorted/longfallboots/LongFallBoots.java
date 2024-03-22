package net.portalmod.common.sorted.longfallboots;

//import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;

import javax.annotation.Nullable;

public class LongFallBoots extends ArmorItem {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/models/armor/longfall_boots.png");
    
    public LongFallBoots(IArmorMaterial armorMaterial, EquipmentSlotType slotType, Properties properties) {
        super(armorMaterial, slotType, properties);
    }
    
    @Nullable
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entity, ItemStack itemStack, EquipmentSlotType armorSlot, A originalModel) {
        return (A)(new LongFallBootsModel(entity));
    }
    
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return TEXTURE.toString();
    }
}