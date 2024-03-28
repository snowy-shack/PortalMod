package net.portalmod.core.init;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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