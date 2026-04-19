package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Fizzleable {
    @Shadow
    public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> p_i48580_1_, World p_i48580_2_) {
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
