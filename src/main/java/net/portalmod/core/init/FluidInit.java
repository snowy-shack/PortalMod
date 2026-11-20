package net.portalmod.core.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.goo.GooFluid;
import net.portalmod.core.registry.DeferredRegister;
import net.portalmod.core.registry.RegistryObject;

public class FluidInit {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, PortalMod.MODID);

    private FluidInit() {}

    public static final RegistryObject<FlowingFluid> GOO_FLUID = FLUIDS.register("goo_fluid", GooFluid.Source::new);
    public static final RegistryObject<FlowingFluid> GOO_FLOWING = FLUIDS.register("goo_flowing", GooFluid.Flowing::new);
}
