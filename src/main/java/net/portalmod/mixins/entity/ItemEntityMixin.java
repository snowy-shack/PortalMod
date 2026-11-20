package net.portalmod.mixins.entity;

import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Fizzleable {
    @Shadow
    public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    @Override
    public boolean shouldCheckForFizzlers() {
        return Fizzleable.isFizzleableItem(this.getItem());
    }

    @Override
    public void onTouchingFizzler() {
        PortalGun.fizzleGunItem(this.getItem());
    }
}
