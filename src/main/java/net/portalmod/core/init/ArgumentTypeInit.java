package net.portalmod.core.init;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.common.commands.LowercaseEnumArgument;

public class ArgumentTypeInit {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerAll() {
        ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(PortalMod.MODID, "lowercase_enum"),
                (Class) LowercaseEnumArgument.class, new LowercaseEnumArgument.Serializer());
    }
}
