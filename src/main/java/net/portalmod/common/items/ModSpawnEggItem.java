package net.portalmod.common.items;

import net.minecraftforge.common.util.Lazy;
import net.portalmod.core.registry.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class ModSpawnEggItem extends SpawnEggItem {
    protected static final List<ModSpawnEggItem> UNADDED = new ArrayList<>();
    private final Lazy<? extends EntityType<?>> supplier;
    public final String tooltipName;
    
    public ModSpawnEggItem(RegistryObject<? extends EntityType<?>> supplier, Properties properties, String tooltipName) {
        super(null, 0xFFFFFF, 0xFFFFFF, properties);
        this.supplier = Lazy.of(supplier::get);
        UNADDED.add(this);
        this.tooltipName = tooltipName;
    }
    
    public static void register() {
        Map<EntityType<?>, SpawnEggItem> eggs = ObfuscationReflectionHelper.getPrivateValue(SpawnEggItem.class, null, "field_195987_b");
        ModDefaultDispenseItemBehavior behaviour = new ModDefaultDispenseItemBehavior();
        
        for(SpawnEggItem spawnEgg : UNADDED) {
            eggs.put(spawnEgg.getType(null), spawnEgg);
            DispenserBlock.registerBehavior(spawnEgg, behaviour);
        }
        
        UNADDED.clear();
    }
    
    @Override
    public EntityType<?> getType(CompoundTag p_208076_1_) {
        return supplier.get();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        if (this.tooltipName != null) {
            ModUtil.addTooltip(this.tooltipName, list);
        }
    }
    
    private static class ModDefaultDispenseItemBehavior implements DispenseItemBehavior {
        public ItemStack dispense(BlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> type = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
            type.spawn(source.getLevel(), stack, null, source.getPos().relative(direction),
                    MobSpawnType.DISPENSER, direction != Direction.DOWN, false);
            stack.shrink(1);
            return stack;
        }
        
        protected ItemStack execute(BlockSource p_82487_1_, ItemStack p_82487_2_) {
            Direction direction = p_82487_1_.getBlockState().getValue(DispenserBlock.FACING);
            Position iposition = DispenserBlock.getDispensePosition(p_82487_1_);
            ItemStack itemstack = p_82487_2_.split(1);
            spawnItem(p_82487_1_.getLevel(), itemstack, 6, direction, iposition);
            return p_82487_2_;
        }
        
        public static void spawnItem(Level p_82486_0_, ItemStack p_82486_1_, int p_82486_2_, Direction p_82486_3_, Position p_82486_4_) {
            double d0 = p_82486_4_.x();
            double d1 = p_82486_4_.y();
            double d2 = p_82486_4_.z();
            if(p_82486_3_.getAxis() == Direction.Axis.Y) {
                d1 = d1 - 0.125D;
            } else {
                d1 = d1 - 0.15625D;
            }
            
            ItemEntity itementity = new ItemEntity(p_82486_0_, d0, d1, d2, p_82486_1_);
            double d3 = p_82486_0_.random.nextDouble() * 0.1D + 0.2D;
            itementity.setDeltaMovement(p_82486_0_.random.nextGaussian() * (double)0.0075F * (double)p_82486_2_ + (double)p_82486_3_.getStepX() * d3, p_82486_0_.random.nextGaussian() * (double)0.0075F * (double)p_82486_2_ + (double)0.2F, p_82486_0_.random.nextGaussian() * (double)0.0075F * (double)p_82486_2_ + (double)p_82486_3_.getStepZ() * d3);
            p_82486_0_.addFreshEntity(itementity);
        }
        
        protected void playSound(BlockSource p_82485_1_) {
            p_82485_1_.getLevel().levelEvent(1000, p_82485_1_.getPos(), 0);
        }
        
        protected void playAnimation(BlockSource p_82489_1_, Direction p_82489_2_) {
            p_82489_1_.getLevel().levelEvent(2000, p_82489_1_.getPos(), p_82489_2_.get3DDataValue());
        }
    }
}