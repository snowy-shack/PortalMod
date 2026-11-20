package net.portalmod.core.init;

import net.portalmod.PortalMod;

import java.util.function.Supplier;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum ArmorMaterialInit implements ArmorMaterial {
    LONGFALL_BOOTS(PortalMod.MODID + ":longfall_boots", 37, new int[]{3, 6, 8, 3}, 15,
            SoundEvents.ARMOR_EQUIP_IRON, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_SCRAP));
    
    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;
    
    private ArmorMaterialInit(String name, int durabilityMultiplier, int[] slotProtections, int enchantmentValue, SoundEvent sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }
    
    public int getDurabilityForSlot(EquipmentSlot p_200896_1_) {
        return HEALTH_PER_SLOT[p_200896_1_.getIndex()] * this.durabilityMultiplier;
    }
    
    public int getDefenseForSlot(EquipmentSlot p_200902_1_) {
        return this.slotProtections[p_200902_1_.getIndex()];
    }
    
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }
    
    public SoundEvent getEquipSound() {
        return this.sound;
    }
    
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return this.name;
    }
    
    public float getToughness() {
        return this.toughness;
    }
    
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}