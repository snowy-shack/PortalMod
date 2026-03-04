package net.portalmod.core.init;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.antline.AntlineTileEntity;
import net.portalmod.common.sorted.cubedropper.CubeDropperTileEntity;
import net.portalmod.common.sorted.door.ChamberDoorTileEntity;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.fizzler.FizzlerEmitterTileEntity;
import net.portalmod.common.sorted.radio.RadioBlockTileEntity;
import net.portalmod.common.sorted.trigger.TriggerTileEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

@SuppressWarnings("DataFlowIssue")
public class TileEntityTypeInit {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PortalMod.MODID);

    public static final RegistryObject<TileEntityType<RadioBlockTileEntity>> RADIO = TILE_ENTITY_TYPES.register("radio",
            () -> TileEntityType.Builder.of(RadioBlockTileEntity::new,
                    getBlocks(TileEntityTypeInit::getRadioBlocks)).build(null));


    public static final RegistryObject<TileEntityType<AntlineTileEntity>> ANTLINE = TILE_ENTITY_TYPES.register("antline",
            () -> TileEntityType.Builder.of(AntlineTileEntity::new,
                    getBlocks(TileEntityTypeInit::getAntlineBlocks)).build(null));


    public static final RegistryObject<TileEntityType<FaithPlateTileEntity>> FAITHPLATE = TILE_ENTITY_TYPES.register("faithplate",
            () -> TileEntityType.Builder.of(FaithPlateTileEntity::new,
                    getBlocks(TileEntityTypeInit::getFaithplateBlocks)).build(null));


    public static final RegistryObject<TileEntityType<ChamberDoorTileEntity>> CHAMBER_DOOR = TILE_ENTITY_TYPES.register("chamber_door",
            () -> TileEntityType.Builder.of(ChamberDoorTileEntity::new,
                    getBlocks(TileEntityTypeInit::getChamberDoorBlocks)).build(null));


    public static final RegistryObject<TileEntityType<CubeDropperTileEntity>> CUBE_DROPPER = TILE_ENTITY_TYPES.register("cube_dropper",
            () -> TileEntityType.Builder.of(CubeDropperTileEntity::new,
                    getBlocks(TileEntityTypeInit::getCubeDropperBlocks)).build(null));


    public static final RegistryObject<TileEntityType<FizzlerEmitterTileEntity>> FIZZLER_EMITTER = TILE_ENTITY_TYPES.register("fizzler_emitter",
            () -> TileEntityType.Builder.of(FizzlerEmitterTileEntity::new,
                    getBlocks(TileEntityTypeInit::getFizzlerEmitterBlocks)).build(null));


    public static final RegistryObject<TileEntityType<TriggerTileEntity>> TRIGGER = TILE_ENTITY_TYPES.register("trigger",
            () -> TileEntityType.Builder.of(TriggerTileEntity::new,
                    getBlocks(TileEntityTypeInit::getTriggerBlocks)).build(null));



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
}