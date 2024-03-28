package net.portalmod.core.init;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;

public class FluidInit {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, PortalMod.MODID);

    private FluidInit() {}

    public static final RegistryObject<FlowingFluid> GOO_FLUID = FLUIDS.register("goo_fluid",
            () -> new ForgeFlowingFluid.Source(FluidInit.GOO_PROPERTIES));

    public static final RegistryObject<FlowingFluid> GOO_FLOWING = FLUIDS.register("goo_flowing",
            () -> new ForgeFlowingFluid.Flowing(FluidInit.GOO_PROPERTIES));

    public static final ForgeFlowingFluid.Properties GOO_PROPERTIES = new ForgeFlowingFluid.Properties(
            () -> GOO_FLUID.get(),
            () -> GOO_FLOWING.get(),
            FluidAttributes.builder(
                    new ResourceLocation(PortalMod.MODID, "block/goo_still"),
                    new ResourceLocation(PortalMod.MODID, "block/goo_flow")
            )
            .translationKey("block.portalmod.goo")
//            .color(0x55544621)
//            .density(2000)
//            .viscosity(3000)
//            .temperature(300)
    )
//    .slopeFindDistance(2)
    .tickRate(20)
    .levelDecreasePerBlock(2)
    .block(() -> BlockInit.GOO.get())
    .canMultiply()
    .bucket(() -> ItemInit.GOO_BUCKET.get());
}