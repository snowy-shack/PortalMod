package net.portalmod.core.init;

import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.portalmod.PortalMod;
import net.portalmod.common.commands.LowercaseEnumArgument;

public class ArgumentTypeInit {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerAll() {
        ArgumentTypes.register(PortalMod.MODID + "lowercase_enum", LowercaseEnumArgument.class, (IArgumentSerializer) new LowercaseEnumArgument.Serializer());
    }
}