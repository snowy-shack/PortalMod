package net.portalmod.core.init;

import net.portalmod.core.registry.RegistryObject;
import net.portalmod.core.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.portalmod.PortalMod;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AttributeInit {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, PortalMod.MODID);

    private AttributeInit() {}

    public static final RegistryObject<Attribute> BUTTON_REACH = register("button_reach", 2, 0, 2048);
    public static final RegistryObject<Attribute> GRAB_REACH = register("grab_reach", 2, 0, 2048);

    private static RegistryObject<Attribute> register(String name, double base, double min, double max) {
        return ATTRIBUTES.register("generic." + name, () -> new RangedAttribute(
                "attribute.name.generic.portalmod." + name, base, min, max).setSyncable(true));
    }
}