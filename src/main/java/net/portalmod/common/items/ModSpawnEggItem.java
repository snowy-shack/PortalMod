package net.portalmod.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ModSpawnEggItem extends SpawnEggItem {
    protected static final List<ModSpawnEggItem> UNADDED = new ArrayList<>();
    private final Lazy<? extends EntityType<?>> supplier;
    
    public ModSpawnEggItem(RegistryObject<? extends EntityType<?>> supplier, int primary, int secondary, Properties properties) {
        super(null, primary, secondary, properties);
        this.supplier = Lazy.of(supplier::get);
        UNADDED.add(this);
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
    public EntityType<?> getType(CompoundNBT p_208076_1_) {
        return supplier.get();
    }
    
    private static class ModDefaultDispenseItemBehavior implements IDispenseItemBehavior {
        public ItemStack dispense(IBlockSource source, ItemStack stack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            EntityType<?> type = ((SpawnEggItem)stack.getItem()).getType(stack.getTag());
            type.spawn(source.getLevel(), stack, null, source.getPos(), SpawnReason.DISPENSER, direction != Direction.DOWN, false);
            stack.shrink(1);
            return stack;
        }
        
        protected ItemStack execute(IBlockSource p_82487_1_, ItemStack p_82487_2_) {
            Direction direction = p_82487_1_.getBlockState().getValue(DispenserBlock.FACING);
            IPosition iposition = DispenserBlock.getDispensePosition(p_82487_1_);
            ItemStack itemstack = p_82487_2_.split(1);
            spawnItem(p_82487_1_.getLevel(), itemstack, 6, direction, iposition);
            return p_82487_2_;
        }
        
        public static void spawnItem(World p_82486_0_, ItemStack p_82486_1_, int p_82486_2_, Direction p_82486_3_, IPosition p_82486_4_) {
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
        
        protected void playSound(IBlockSource p_82485_1_) {
            p_82485_1_.getLevel().levelEvent(1000, p_82485_1_.getPos(), 0);
        }
        
        protected void playAnimation(IBlockSource p_82489_1_, Direction p_82489_2_) {
            p_82489_1_.getLevel().levelEvent(2000, p_82489_1_.getPos(), p_82489_2_.get3DDataValue());
        }
    }
}