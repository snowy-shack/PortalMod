package net.portalmod.core.init;

import net.portalmod.PortalMod;
import net.portalmod.common.commands.LowercaseEnumArgument;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.ArgumentSerializer;

public class ArgumentTypeInit {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerAll() {
        ArgumentTypes.register(PortalMod.MODID + "lowercase_enum", LowercaseEnumArgument.class, (ArgumentSerializer) new LowercaseEnumArgument.Serializer());
    }
}