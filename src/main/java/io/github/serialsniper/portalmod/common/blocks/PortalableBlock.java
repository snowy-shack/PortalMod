package io.github.serialsniper.portalmod.common.blocks;

import java.util.*;

import io.github.serialsniper.portalmod.client.particles.PortalParticle;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.common.blockentities.PortalableBlockTileEntity;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.StateContainer.*;
import net.minecraft.state.properties.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.*;

public class PortalableBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<PortalEnd> END = EnumProperty.create("end", PortalEnd.class);
    private final BlockState DEFAULT = this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false);
    public static VoxelShape[] AABB_LOWER = new VoxelShape[4];
    public static VoxelShape[] AABB_UPPER = new VoxelShape[4];
    private static VoxelShape[] AABB_ACTIVE = new VoxelShape[4];
    public PortalableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(DEFAULT);
        this.createVoxelShapes();
    }
    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
//        if(level.isClientSide)
//            PortalParticle.spawnBurst((ClientWorld)level, ray.getLocation(), ray.getDirection());
        
//		level.addParticle(new PortalParticle.PortalParticleData(1, 0, 0, 1),
//				pos.getX() + .5f, pos.getY() + 1.5f, pos.getZ() + .5f, 0, .05f, 0);
//		Random random = new Random();
//
//		for(int j = 0; j < 100; ++j) {
//			double d23 = random.nextDouble() * 4.0D;
//			double d27 = random.nextDouble() * Math.PI * 2.0D;
//			double d29 = Math.cos(d27) * d23;
//			double d5 = 0.01D + random.nextDouble() * 0.5D;
//			double d7 = Math.sin(d27) * d23;
//
//			Vector3d vector3d = Vector3d.atBottomCenterOf(pos).add(0, 1, 0);
//			level.addParticle(new PortalParticle.PortalParticleData(1, 0, 0, 1), vector3d.x + d29 * 0.1D, vector3d.y + 0.3D, vector3d.z + d7 * 0.1D, d29, d5, d7);
//
////			if (particle1 != null) {
////				float f2 = 0.75F + random.nextFloat() * 0.25F;
////				particle1.setColor(f3 * f2, f4 * f2, f5 * f2);
////				particle1.setPower((float)d23);
////			}
//		}

        return super.use(state, level, pos, player, hand, ray);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypeInit.PORTABLE_BLOCK.get().create();
    }

    private final float thickness = 0.001f;

    private void createVoxelShapes() {
        {
            VoxelShape top = 	Block.box(0, 16, 0, 16, 16 - thickness, 16);
            VoxelShape bottom = Block.box(0, 0, 0, 16, thickness, 16);
            VoxelShape right = 	Block.box(0, 0, 0, thickness, 16, 16);
            VoxelShape left = 	Block.box(16, 0, 0, 16 - thickness, 16, 16);
            VoxelShape front = 	Block.box(0, 0, 0, 16, 16, thickness);
            VoxelShape back = 	Block.box(0, 0, 16, 16, 16, 16 - thickness);

            AABB_LOWER[0] = VoxelShapes.or(bottom, back, left, right);
            AABB_LOWER[1] = VoxelShapes.or(bottom, front, left, right);
            AABB_LOWER[2] = VoxelShapes.or(bottom, left, front, back);
            AABB_LOWER[3] = VoxelShapes.or(bottom, right, front, back);

            AABB_UPPER[0] = VoxelShapes.or(top, back, left, right);
            AABB_UPPER[1] = VoxelShapes.or(top, front, left, right);
            AABB_UPPER[2] = VoxelShapes.or(top, left, front, back);
            AABB_UPPER[3] = VoxelShapes.or(top, right, front, back);
        }

        {
//			VoxelShape top = 	Block.box(-16, 32, 16, 32, 48, 0);
//			VoxelShape bottom = Block.box(-16, -16, 16, 32, 0, 0);
//			VoxelShape right = 	Block.box(16, 0, 16, 32, 32, 0);
//			VoxelShape left = 	Block.box(-16, 0, 16, 0, 32, 0);
//			VoxelShape back = 	Block.box(0, 0, 0, 16, 32, -16);
            VoxelShape top = 	Block.box(0, 32, 0, 16, 48, 16);
            VoxelShape bottom = Block.box(0, -16, 0, 16, 0, 16);
            VoxelShape east = 	Block.box(16, 0, 0, 32, 32, 16);
            VoxelShape west = 	Block.box(-16, 0, 0, 0, 32, 16);
            VoxelShape north = 	Block.box(0, 0, -16, 16, 32, 0);
            VoxelShape south = 	Block.box(0, 0, 16, 16, 32, 32);

            AABB_ACTIVE[0] = VoxelShapes.or(top, bottom, east, west, south);
            AABB_ACTIVE[1] = VoxelShapes.or(top, bottom, east, west, north);
            AABB_ACTIVE[2] = VoxelShapes.or(top, bottom, north, south, east);
            AABB_ACTIVE[3] = VoxelShapes.or(top, bottom, north, south, west);
        }
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ACTIVE, HALF, END);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(ACTIVE) ? super.getLightValue(state, world, pos) : 0;
    }

    private boolean isColliding(PlayerEntity player, BlockPos pos) {
        VoxelShape voxelshape = VoxelShapes.block().move(pos.getX(), pos.getY(), pos.getZ());
        return VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(player.getBoundingBox()), IBooleanFunction.AND);
    }

    private boolean isCollidingTall(Entity player, BlockPos pos) {
        VoxelShape voxelshape = VoxelShapes.block().move(pos.getX(), pos.getY(), pos.getZ());
        VoxelShape voxelshape2 = VoxelShapes.block().move(pos.getX(), pos.getY() + 1, pos.getZ());
        if(player == null)
            return true;
        return VoxelShapes.joinIsNotEmpty(VoxelShapes.or(voxelshape, voxelshape2), VoxelShapes.create(player.getBoundingBox()), IBooleanFunction.AND);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
        if(!state.getValue(ACTIVE))
            return VoxelShapes.block();

        Entity entity = selectionContext.getEntity();

        if(entity instanceof PlayerEntity || entity instanceof MinecartEntity) {
            BlockPos front = pos.relative(state.getValue(FACING));

            if(state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                if(isCollidingTall(entity, front) || isCollidingTall(entity, pos))
                    return AABB_ACTIVE[state.getValue(FACING).get3DDataValue() - 2];
            } else {
                if(isCollidingTall(entity, front.below()) || isCollidingTall(entity, pos.below()))
                    return AABB_ACTIVE[state.getValue(FACING).get3DDataValue() - 2].move(0, -1, 0);
            }
        }

        return VoxelShapes.block();
    }

    @Override
    public void entityInside(BlockState blockState, World level, BlockPos pos, Entity entity) {
//		System.out.println("inside");

        if(!(entity instanceof PlayerEntity))
            return;

//		System.out.println("player inside");

        PlayerEntity player = (PlayerEntity) entity;
        Vector3d position = player.position();
        BlockPos thisPos = player.blockPosition();
        BlockState state = level.getBlockState(player.blockPosition());

        Vector3d oldPosition = player.getPosition(0);
        Vector3d newPosition = player.getPosition(1);

//		System.out.println("1");

//		System.out.println(player.getMainHandItem().getItem() instanceof PortalGun);
//		System.out.println(state.getBlock() instanceof PortalableBlock);
//		System.out.println(state.getValue(PortalableBlock.ACTIVE));
//		System.out.println(state.getValue(PortalableBlock.HALF) == DoubleBlockHalf.LOWER);
//		System.out.println(isCollidingTall(player, pos));

        if(player.getMainHandItem().getItem() instanceof PortalGun
                && state.getBlock() instanceof PortalableBlock
                && state.getValue(PortalableBlock.ACTIVE)
                && state.getValue(PortalableBlock.HALF) == DoubleBlockHalf.LOWER
                && isCollidingTall(player, pos)

                && player.blockPosition().getX() == pos.getX()
                && player.blockPosition().getY() == pos.getY()
                && player.blockPosition().getZ() == pos.getZ()) {

//			System.out.println("2");

            BlockPos otherPos = PortalGun.getPortalPosition(player.getMainHandItem(),
                    state.getValue(PortalableBlock.END).other(), level);

            Direction side = level.getBlockState(thisPos).getValue(PortalableBlock.FACING);
            Direction otherSide = level.getBlockState(otherPos).getValue(PortalableBlock.FACING);
//			thisPos = thisPos.offset(side.getNormal());

            float yRotDeg0 = ((180 + (otherSide.toYRot() - side.toYRot())) % 360);
            float yRotDeg = yRotDeg0;
            if(yRotDeg % 180 != 0)
                yRotDeg = -yRotDeg % 360;
            float yRot = yRotDeg * (float)(Math.PI / 180.0f);

//			System.out.println("3");

//			yRot %= 360;
//			if (yRot < 0)
//				yRot += 360;

//			int xVector = yRot > 90 ? 1 : -1;
//			int zVector = (yRot + 90) % 360 > 90 ? 1 : -1;

//			double xr = position.x();
//			double zr = position.z();

            Vector3i sideNormal = side.getNormal();
            Vector3d thisPosVec = new Vector3d(thisPos.getX(), thisPos.getY(), thisPos.getZ())
                    .add(.5f, .5f, .5f)
                    .add((float)sideNormal.getX() * .5f, (float)sideNormal.getY() * .5f, (float)sideNormal.getZ() * .5f);

            Vector3i otherSideNormal = otherSide.getNormal();
            Vector3d otherPosVec = new Vector3d(otherPos.getX(), otherPos.getY(), otherPos.getZ())
                    .add(.5f, .5f, .5f)
                    .add((float)otherSideNormal.getX() * .5f, (float)otherSideNormal.getY() * .5f, (float)otherSideNormal.getZ() * .5f);

//			double xDist = xr - (thisPos.getX() + 0.5d);
//			double zDist = zr - (thisPos.getZ() + 0.5d);
            double xDist = position.x() - thisPosVec.x();
            double zDist = position.z() - thisPosVec.z();
            double dist = Math.sqrt(Math.pow(xDist, 2) + Math.pow(zDist, 2));
            double angle = Math.atan2(xDist, zDist) + yRot;

            double oldXDist = oldPosition.x() - thisPosVec.x();
            double oldZDist = oldPosition.z() - thisPosVec.z();
            double oldDist = Math.sqrt(Math.pow(oldXDist, 2) + Math.pow(oldZDist, 2));
            double oldAngle = Math.atan2(oldXDist, oldZDist) + yRot;

            double newXDist = newPosition.x() - thisPosVec.x();
            double newZDist = newPosition.z() - thisPosVec.z();
            double newDist = Math.sqrt(Math.pow(newXDist, 2) + Math.pow(newZDist, 2));
            double newAngle = Math.atan2(newXDist, newZDist) + yRot;

//			if (yRot % 180 == 90) {
//				xr = position.z();
//				zr = position.x();
//			}

//			double x = xDist * Math.cos(angle) + (otherPos.getX() + 0.5d);
//			double y = position.y() + (otherPos.getY() - thisPos.getY());
//			double z = zDist * Math.sin(angle) + (otherPos.getZ() + 0.5d);

            double x = dist * Math.sin(angle) + otherPosVec.x();
            double y = position.y() + (otherPos.getY() - thisPos.getY());
            double z = dist * Math.cos(angle) + otherPosVec.z();

            double oldX = oldDist * Math.sin(oldAngle) + otherPosVec.x();
            double oldY = oldPosition.y() + (otherPos.getY() - thisPos.getY());
            double oldZ = oldDist * Math.cos(oldAngle) + otherPosVec.z();

            double newX = newDist * Math.sin(newAngle) + otherPosVec.x();
            double newY = newPosition.y() + (otherPos.getY() - thisPos.getY());
            double newZ = newDist * Math.cos(newAngle) + otherPosVec.z();

//			System.out.println("4");

//			x = player.xo;
//			y = player.yo;
//			z = player.zo + 3d;

//			double xOld = player.xOld;
//			double yOld = player.yOld;
//			double zOld = player.zOld + 3d;

//			System.out.println(otherPos.getY() - thisPos.getY());

//			Minecraft.getInstance().player.absMoveTo(x, y, z, Minecraft.getInstance().player.yRot + (yRot - 180), Minecraft.getInstance().player.xRot);

            player = Minecraft.getInstance().player;

            player.xo = oldX;
            player.yo = oldY;
            player.zo = oldZ;
            player.xOld = oldX;
            player.yOld = oldY;
            player.zOld = oldZ;

            player.setPos(newX, newY, newZ);

//			player.xRot = MathHelper.clamp(p_70080_8_, -90.0F, 90.0F) % 360.0F;
//			player.xRotO = this.xRot;
            System.out.println(player.yRot % 360 + " ; " + player.yRotO % 360);
//			player.yRot = (player.yRot + 90) % 360;
//			player.yRotO = (player.yRotO + 90) % 360;
            player.yRot = (player.yRot + yRotDeg0) % 360;
            player.yRotO = (player.yRotO + yRotDeg0) % 360;
//			player.yRot = 0;
//			player.yRotO = 0;

//			System.out.println("5");

//			player.yHeadRot = (player.yHeadRot + (yRotDeg0 - 180)) % 360.0F;
//			player.yHeadRotO = (player.yHeadRotO + (yRotDeg0 - 180)) % 360.0F;

//			double hypotenuse = player.getDeltaMovement().distanceTo(Vector3d.ZERO);
            Vector3d deltaMovementOld = player.getDeltaMovement();
//			double hypothenuse = deltaMovementOld.distanceTo(Vector3d.ZERO);

//			Vector3d deltaMovementNew = new Vector3d(
//					Math.cos(yRotDeg * (Math.PI / 180)) * hypothenuse,
//					deltaMovementOld.y,
//					Math.sin(yRotDeg * (Math.PI / 180)) * hypothenuse);

            double yRotRad = yRotDeg * (Math.PI / 180);
            Vector3d deltaMovementNew = new Vector3d(
                    deltaMovementOld.x * Math.cos(yRotRad) - deltaMovementOld.y * Math.sin(yRotRad),
                    0,
                    deltaMovementOld.x * Math.sin(yRotRad) + deltaMovementOld.y * Math.cos(yRotRad)
            );

//			Vector3d deltaMovementNew = deltaMovementOld.multiply(
//					Math.cos((3 - otherSide.get2DDataValue()) * (Math.PI / 2)),
//					1,
//					Math.sin((3 - otherSide.get2DDataValue()) * (Math.PI / 2)));
//			Vector3d deltaMovementNew = deltaMovementOld.multiply(
//					Math.cos(otherSide.toYRot() * (Math.PI / 180)), 1, Math.sin(otherSide.toYRot() * (Math.PI / 180)));
//			player.setDeltaMovement(deltaMovementNew);

//			double xCollision = Math.abs(pos.getX() + 0.5d - position.x());
//			double zCollision = Math.abs(pos.getZ() + 0.5d - position.z());

//			if(xCollision <= 0.5d && zCollision <= 0.5d) {
//				done = true;
//
//				Vector3d playerPos = Minecraft.getInstance().player.position();
//				BlockPos otherPos = PortalGun.getPortalPosition(Minecraft.getInstance().player.getMainHandItem(),
//						blockState.getValue(PortalableBlock.END).other());
//
//				Direction side = Minecraft.getInstance().level.getBlockState(pos).getValue(PortalableBlock.FACING);
//				Direction otherSide = Minecraft.getInstance().level.getBlockState(otherPos).getValue(PortalableBlock.FACING);
//				pos = pos.offset(side.getNormal());
//
//				float yRot = otherSide.toYRot() - side.toYRot();
//
//				yRot %= 360;
//				if(yRot < 0)
//					yRot += 360;
//
//				int xVector = yRot > 90 ? 1 : -1;
//				int zVector = (yRot + 90) % 360 > 90 ? 1 : -1;
//
//				double xr = playerPos.x();
//				double zr = playerPos.z();
//
//				if(yRot % 180 == 90) {
//					xr = playerPos.z();
//					zr = playerPos.x();
//				}
//
//				double x = (xr - (pos.getX() + 0.5d)) * xVector + (otherPos.getX() + 0.5d);
//				double y = playerPos.y() + (otherPos.getY() - pos.getY());
//				double z = (zr - (pos.getZ() + 0.5d)) * zVector + (otherPos.getZ() + 0.5d);
//
//				System.out.println("\nPLAYER AT [" +
//						Minecraft.getInstance().player.position().x() + ", " +
//						Minecraft.getInstance().player.position().y() + ", " +
//						Minecraft.getInstance().player.position().z()
//						+ "] \nWITH COLLISION [" +
//						xCollision + ", " + zCollision
//						+ "] \nAT BLOCKPOS [" +
//						pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
//						+ "] \nTELEPORTED TO [" +
//						x + ", " + y + ", " + z);
//
//				Minecraft.getInstance().player.absMoveTo(x, y, z, Minecraft.getInstance().player.yRot + yRot, Minecraft.getInstance().player.xRot);
//			}
        }
    }

    private BlockPos clockWise(BlockPos pos, Direction side) {
        switch(side) {
            case NORTH:
                return pos.east();
            case SOUTH:
                return pos.west();
            case WEST:
                return pos.north();
            case EAST:
                return pos.south();
            default:
                return pos;
        }
    }

    private BlockPos counterClockWise(BlockPos pos, Direction side) {
        switch(side) {
            case NORTH:
                return pos.west();
            case SOUTH:
                return pos.east();
            case WEST:
                return pos.south();
            case EAST:
                return pos.north();
            default:
                return pos;
        }
    }

    public BlockPos createPortal(ItemStack stack, PortalEnd end, Direction side, World world, BlockPos pos, UUID gunUUID, BlockRayTraceResult rayHit) {
        if(world.isClientSide())
            return null;

        if(world.getBlockState(pos).getValue(ACTIVE) && world.getBlockState(pos).getValue(HALF) == DoubleBlockHalf.UPPER
                && world.getBlockState(pos.above()).getBlock() instanceof PortalableBlock && world.getBlockState(pos.above().above()).getBlock() instanceof PortalableBlock) {
            pos = pos.above();

        } else if(world.getBlockState(pos.above()).getValue(ACTIVE) && world.getBlockState(pos.above()).getValue(HALF) == DoubleBlockHalf.LOWER
                && world.getBlockState(pos.below()).getBlock() instanceof PortalableBlock && world.getBlockState(pos.below().below()).getBlock() instanceof PortalableBlock) {
            pos = pos.below();

        } else if(world.getBlockState(pos).getValue(ACTIVE) || world.getBlockState(pos.above()).getValue(ACTIVE)) {
            double x = rayHit.getLocation().x - rayHit.getBlockPos().getX();
            double z = rayHit.getLocation().z - rayHit.getBlockPos().getZ();
            double current = z;

            if(side == Direction.NORTH || side == Direction.SOUTH)
                current = x;

            if(side == Direction.NORTH || side == Direction.EAST)
                current = 1 - current;

            BlockPos posNew = pos;
            BlockPos posAlt = pos;

            if(current > 0.5D) {
                posNew = counterClockWise(pos, side);
                posAlt = clockWise(pos, side);
            } else {
                posNew = clockWise(pos, side);
                posAlt = counterClockWise(pos, side);
            }

            if(world.getBlockState(posNew).getBlock() instanceof PortalableBlock && world.getBlockState(posNew.above()).getBlock() instanceof PortalableBlock)
                pos = posNew;
            else if(world.getBlockState(posAlt).getBlock() instanceof PortalableBlock && world.getBlockState(posAlt.above()).getBlock() instanceof PortalableBlock)
                pos = posAlt;
            else return null;
        }

        BlockState stateNew = DEFAULT.setValue(ACTIVE, true).setValue(FACING, side).setValue(END, end);
        world.setBlock(pos, stateNew.setValue(HALF, DoubleBlockHalf.LOWER), 3);
        world.setBlock(pos.above(), stateNew.setValue(HALF, DoubleBlockHalf.UPPER), 3);

        ((PortalableBlockTileEntity)world.getBlockEntity(pos)).uuid = gunUUID;
        ((PortalableBlockTileEntity)world.getBlockEntity(pos.above())).uuid = gunUUID;

        return pos;
    }

    public void fizzlePortal(World world, BlockPos pos) {
        if(world.isClientSide())
            return;

        world.setBlock(pos, DEFAULT, 3);
        world.setBlock(pos.above(), DEFAULT, 3);

        ((PortalableBlockTileEntity)world.getBlockEntity(pos)).uuid = null;
        ((PortalableBlockTileEntity)world.getBlockEntity(pos.above())).uuid = null;
    }
}