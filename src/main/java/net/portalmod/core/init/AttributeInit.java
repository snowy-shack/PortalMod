package net.portalmod.core.init;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;

public class AttributeInit {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PortalMod.MODID);

    private AttributeInit() {}

    public static final RegistryObject<Attribute> BUTTON_REACH = register("button_reach", 2, 0, 2048);
    public static final RegistryObject<Attribute> GRAB_REACH = register("grab_reach", 2, 0, 2048);

    private static RegistryObject<Attribute> register(String name, double base, double min, double max) {
        return ATTRIBUTES.register("generic." + name, () -> new RangedAttribute(
                "attribute.name.generic.portalmod." + name, base, min, max).setSyncable(true));
    }
}