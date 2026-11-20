package net.portalmod.core.init;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.portalmod.core.registry.RegistryObject;
import net.portalmod.core.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.portalmod.PortalMod;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class FluidInit {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, PortalMod.MODID);

    private FluidInit() {}

    public static final RegistryObject<FlowingFluid> GOO_FLUID = FLUIDS.register("goo_fluid",
            () -> new ForgeFlowingFluid.Source(FluidInit.GOO_PROPERTIES));

    public static final RegistryObject<FlowingFluid> GOO_FLOWING = FLUIDS.register("goo_flowing",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.GOO_PROPERTIES));

    public static final ForgeFlowingFluid.Properties GOO_PROPERTIES = new ForgeFlowingFluid.Properties(GOO_FLUID, GOO_FLOWING,
            FluidAttributes.builder(
                    new ResourceLocation(PortalMod.MODID, "block/goo_still"),
                    new ResourceLocation(PortalMod.MODID, "block/goo_flow")
                    )
                    .translationKey("block.portalmod.goo")
                    .sound(new SoundEvent(new ResourceLocation(PortalMod.MODID, "item.bucket.fill_goo")), new SoundEvent(new ResourceLocation(PortalMod.MODID, "item.bucket.empty_goo")))
//                    .color(0x55544621)
//                    .density(2000)
//                    .viscosity(3000)
//                    .temperature(300)

    )
//            .slopeFindDistance(2)
            .tickRate(20)
            .levelDecreasePerBlock(2)
            .block(BlockInit.GOO)
            .canMultiply()
            .bucket(ItemInit.GOO_BUCKET);

    public static DamageSource GOO_DAMAGE = new DamageSource("goo");
}