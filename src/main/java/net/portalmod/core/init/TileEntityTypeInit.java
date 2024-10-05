package net.portalmod.core.init;

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

public class TileEntityTypeInit {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PortalMod.MODID);

    private TileEntityTypeInit() {}
    
    public static final RegistryObject<TileEntityType<RadioBlockTileEntity>> RADIO = TILE_ENTITY_TYPES.register("radio",
            () -> TileEntityType.Builder.of(RadioBlockTileEntity::new, BlockInit.RADIO.get()).build(null));

    public static final RegistryObject<TileEntityType<AntlineTileEntity>> ANTLINE = TILE_ENTITY_TYPES.register("antline",
            () -> TileEntityType.Builder.of(AntlineTileEntity::new, BlockInit.ANTLINE.get()).build(null));

    public static final RegistryObject<TileEntityType<FaithPlateTileEntity>> FAITHPLATE = TILE_ENTITY_TYPES.register("faithplate",
            () -> TileEntityType.Builder.of(FaithPlateTileEntity::new, BlockInit.FAITHPLATE.get()).build(null));

    public static final RegistryObject<TileEntityType<ChamberDoorTileEntity>> CHAMBER_DOOR = TILE_ENTITY_TYPES.register("chamber_door",
            () -> TileEntityType.Builder.of(ChamberDoorTileEntity::new, BlockInit.CHAMBER_DOOR.get()).build(null));

    public static final RegistryObject<TileEntityType<CubeDropperTileEntity>> CUBE_DROPPER = TILE_ENTITY_TYPES.register("cube_dropper",
            () -> TileEntityType.Builder.of(CubeDropperTileEntity::new, BlockInit.CUBE_DROPPER.get()).build(null));

    public static final RegistryObject<TileEntityType<FizzlerEmitterTileEntity>> FIZZLER_EMITTER = TILE_ENTITY_TYPES.register("fizzler_emitter",
            () -> TileEntityType.Builder.of(FizzlerEmitterTileEntity::new, BlockInit.FIZZLER_EMITTER.get()).build(null));
}