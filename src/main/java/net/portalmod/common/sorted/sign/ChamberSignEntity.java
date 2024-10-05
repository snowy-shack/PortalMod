package net.portalmod.common.sorted.sign;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.EntityInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.packet.SSpawnChamberSignPacket;

import javax.annotation.Nullable;

public class ChamberSignEntity extends HangingEntity {

    public static final DataParameter<Integer> DATA_LEFT_DIGIT = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.INT);
    public static final DataParameter<Integer> DATA_RIGHT_DIGIT = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.INT);
    public static final DataParameter<Integer> DATA_PROGRESS = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.INT);
    public static final DataParameter<Boolean> DATA_ICON_1 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_2 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_3 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_4 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_5 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_6 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_7 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> DATA_ICON_8 = EntityDataManager.defineId(ChamberSignEntity.class, DataSerializers.BOOLEAN);

    public ChamberSignEntity(EntityType<? extends HangingEntity> p_i48561_1_, World p_i48561_2_) {
        super(p_i48561_1_, p_i48561_2_);
    }

    public ChamberSignEntity(World world, BlockPos pos, Direction direction) {
        super(EntityInit.CHAMBER_SIGN.get(), world, pos);
        this.setDirection(direction);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        Entity entity = damageSource.getEntity();
        if (entity instanceof PlayerEntity && !((PlayerEntity) entity).isCreative() && !(((PlayerEntity) entity).getMainHandItem().getItem() instanceof WrenchItem)) {
            return false;
        }

        return super.hurt(damageSource, damage);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_LEFT_DIGIT, 0);
        this.getEntityData().define(DATA_RIGHT_DIGIT, 0);
        this.getEntityData().define(DATA_PROGRESS, 6);
        this.getEntityData().define(DATA_ICON_1, true);
        this.getEntityData().define(DATA_ICON_2, false);
        this.getEntityData().define(DATA_ICON_3, true);
        this.getEntityData().define(DATA_ICON_4, false);
        this.getEntityData().define(DATA_ICON_5, false);
        this.getEntityData().define(DATA_ICON_6, false);
        this.getEntityData().define(DATA_ICON_7, true);
        this.getEntityData().define(DATA_ICON_8, true);
    }

    @Override
    protected void recalculateBoundingBox() {
        // Copy from HangingEntity
        if (this.direction != null) {
            double x = (double)this.pos.getX() + 0.5D;
            double y = (double)this.pos.getY() + 0.5D;
            double z = (double)this.pos.getZ() + 0.5D;

            double widthOffset = this.offs(this.getWidth()) + 0.5;  // shift a lil
            double heightOffset = this.offs(this.getHeight()) + 0.5;

            x = x - (double)this.direction.getStepX() * (7.0 / 16.0);   // the middle is 7 pixels towards the wall
            z = z - (double)this.direction.getStepZ() * (7.0 / 16.0);
            y = y + heightOffset;

            Direction direction = this.direction.getCounterClockWise();
            x = x + widthOffset * (double)direction.getStepX();
            z = z + widthOffset * (double)direction.getStepZ();

            this.setPosRaw(x, y, z);

            double dx = this.getWidth();
            double dy = this.getHeight();
            double dz = this.getWidth();

            if (this.direction.getAxis() == Direction.Axis.Z) {   // 2 pixels thick
                dz = 2.0D;
            } else {
                dx = 2.0D;
            }

            dx = dx / 32.0D;
            dy = dy / 32.0D;
            dz = dz / 32.0D;

            this.setBoundingBox(new AxisAlignedBB(x - dx, y - dy, z - dz, x + dx, y + dy, z + dz));
        }
    }

    public double offs(int p_190202_1_) {
        return p_190202_1_ % 32 == 0 ? 0.5D : 0.0D;
    }

    @Override
    public int getWidth() {
        return 24;
    }

    @Override
    public int getHeight() {
        return 48;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);

        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative() || !this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }

        this.spawnAtLocation(ItemInit.CHAMBER_SIGN.get());;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1, 1);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return new SSpawnChamberSignPacket(this.getId(), this.getUUID(), this.getPos(), this.direction);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("Facing", (byte) this.direction.get2DDataValue());
        nbt.putInt("LeftDigit", this.getLeftDigit());
        nbt.putInt("RightDigit", this.getRightDigit());
        nbt.putInt("Progress", this.getProgress());
        nbt.putBoolean("Icon1", this.isIcon1());
        nbt.putBoolean("Icon2", this.isIcon2());
        nbt.putBoolean("Icon3", this.isIcon3());
        nbt.putBoolean("Icon4", this.isIcon4());
        nbt.putBoolean("Icon5", this.isIcon5());
        nbt.putBoolean("Icon6", this.isIcon6());
        nbt.putBoolean("Icon7", this.isIcon7());
        nbt.putBoolean("Icon8", this.isIcon8());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        this.setDirection(Direction.from2DDataValue(nbt.getByte("Facing")));
        this.setLeftDigit(nbt.getInt("LeftDigit"));
        this.setRightDigit(nbt.getInt("RightDigit"));
        this.setProgress(nbt.getInt("Progress"));
        this.setIcon1(nbt.getBoolean("Icon1"));
        this.setIcon2(nbt.getBoolean("Icon2"));
        this.setIcon3(nbt.getBoolean("Icon3"));
        this.setIcon4(nbt.getBoolean("Icon4"));
        this.setIcon5(nbt.getBoolean("Icon5"));
        this.setIcon6(nbt.getBoolean("Icon6"));
        this.setIcon7(nbt.getBoolean("Icon7"));
        this.setIcon8(nbt.getBoolean("Icon8"));
    }


    public int getLeftDigit() {
        return this.getEntityData().get(DATA_LEFT_DIGIT);
    }

    public void setLeftDigit(int leftDigit) {
        this.getEntityData().set(DATA_LEFT_DIGIT, leftDigit);
    }

    public int getRightDigit() {
        return this.getEntityData().get(DATA_RIGHT_DIGIT);
    }

    public void setRightDigit(int rightDigit) {
        this.getEntityData().set(DATA_RIGHT_DIGIT, rightDigit);
    }

    public int getProgress() {
        return this.getEntityData().get(DATA_PROGRESS);
    }

    public void setProgress(int progress) {
        this.getEntityData().set(DATA_PROGRESS, progress);
    }

    public boolean isIcon1() {
        return this.getEntityData().get(DATA_ICON_1);
    }

    public void setIcon1(boolean icon1) {
        this.getEntityData().set(DATA_ICON_1, icon1);
    }

    public boolean isIcon2() {
        return this.getEntityData().get(DATA_ICON_2);
    }

    public void setIcon2(boolean icon2) {
        this.getEntityData().set(DATA_ICON_2, icon2);
    }

    public boolean isIcon3() {
        return this.getEntityData().get(DATA_ICON_3);
    }

    public void setIcon3(boolean icon3) {
        this.getEntityData().set(DATA_ICON_3, icon3);
    }

    public boolean isIcon4() {
        return this.getEntityData().get(DATA_ICON_4);
    }

    public void setIcon4(boolean icon4) {
        this.getEntityData().set(DATA_ICON_4, icon4);
    }

    public boolean isIcon5() {
        return this.getEntityData().get(DATA_ICON_5);
    }

    public void setIcon5(boolean icon5) {
        this.getEntityData().set(DATA_ICON_5, icon5);
    }

    public boolean isIcon6() {
        return this.getEntityData().get(DATA_ICON_6);
    }

    public void setIcon6(boolean icon6) {
        this.getEntityData().set(DATA_ICON_6, icon6);
    }

    public boolean isIcon7() {
        return this.getEntityData().get(DATA_ICON_7);
    }

    public void setIcon7(boolean icon7) {
        this.getEntityData().set(DATA_ICON_7, icon7);
    }

    public boolean isIcon8() {
        return this.getEntityData().get(DATA_ICON_8);
    }

    public void setIcon8(boolean icon8) {
        this.getEntityData().set(DATA_ICON_8, icon8);
    }
}
