package net.portalmod.core.init;

import net.portalmod.core.registry.RegistryObject;
import net.portalmod.core.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.antline.AntlineTileEntity;
import net.portalmod.common.sorted.autoportal.AutoPortalTileEntity;
import net.portalmod.common.sorted.cubedropper.CubeDropperTileEntity;
import net.portalmod.common.sorted.door.ChamberDoorTileEntity;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.fizzler.FizzlerEmitterTileEntity;
import net.portalmod.common.sorted.radio.RadioBlockTileEntity;
import net.portalmod.common.sorted.trigger.TriggerTileEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("DataFlowIssue")
public class TileEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PortalMod.MODID);

    public static final RegistryObject<BlockEntityType<RadioBlockTileEntity>> RADIO = TILE_ENTITY_TYPES.register("radio",
            () -> BlockEntityType.Builder.of(RadioBlockTileEntity::new,
                    getBlocks(TileEntityTypeInit::getRadioBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<AntlineTileEntity>> ANTLINE = TILE_ENTITY_TYPES.register("antline",
            () -> BlockEntityType.Builder.of(AntlineTileEntity::new,
                    getBlocks(TileEntityTypeInit::getAntlineBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<FaithPlateTileEntity>> FAITHPLATE = TILE_ENTITY_TYPES.register("faithplate",
            () -> BlockEntityType.Builder.of(FaithPlateTileEntity::new,
                    getBlocks(TileEntityTypeInit::getFaithplateBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<ChamberDoorTileEntity>> CHAMBER_DOOR = TILE_ENTITY_TYPES.register("chamber_door",
            () -> BlockEntityType.Builder.of(ChamberDoorTileEntity::new,
                    getBlocks(TileEntityTypeInit::getChamberDoorBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<CubeDropperTileEntity>> CUBE_DROPPER = TILE_ENTITY_TYPES.register("cube_dropper",
            () -> BlockEntityType.Builder.of(CubeDropperTileEntity::new,
                    getBlocks(TileEntityTypeInit::getCubeDropperBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<FizzlerEmitterTileEntity>> FIZZLER_EMITTER = TILE_ENTITY_TYPES.register("fizzler_emitter",
            () -> BlockEntityType.Builder.of(FizzlerEmitterTileEntity::new,
                    getBlocks(TileEntityTypeInit::getFizzlerEmitterBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<TriggerTileEntity>> TRIGGER = TILE_ENTITY_TYPES.register("trigger",
            () -> BlockEntityType.Builder.of(TriggerTileEntity::new,
                    getBlocks(TileEntityTypeInit::getTriggerBlocks)).build(null));


    public static final RegistryObject<BlockEntityType<AutoPortalTileEntity>> AUTOPORTAL = TILE_ENTITY_TYPES.register("autoportal",
            () -> BlockEntityType.Builder.of(AutoPortalTileEntity::new,
                    getBlocks(TileEntityTypeInit::getAutoPortalBlocks)).build(null));



    public static Block[] getBlocks(UnaryOperator<Set<Block>> function) {
        return function.apply(new HashSet<>()).toArray(new Block[]{});
    }

    public static Set<Block> getRadioBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.RADIO.get());
        return blocks;
    }

    public static Set<Block> getAntlineBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.ANTLINE.get());
        return blocks;
    }

    public static Set<Block> getFaithplateBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.FAITHPLATE.get());
        return blocks;
    }

    public static Set<Block> getChamberDoorBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.CHAMBER_DOOR.get());
        return blocks;
    }

    public static Set<Block> getCubeDropperBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.CUBE_DROPPER.get());
        return blocks;
    }

    public static Set<Block> getFizzlerEmitterBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.FIZZLER_EMITTER.get());
        return blocks;
    }

    public static Set<Block> getTriggerBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.TRIGGER.get());
        return blocks;
    }

    public static Set<Block> getAutoPortalBlocks(Set<Block> blocks) {
        blocks.add(BlockInit.AUTOPORTAL.get());
        return blocks;
    }
}