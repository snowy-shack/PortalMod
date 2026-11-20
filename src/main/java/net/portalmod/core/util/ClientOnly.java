package net.portalmod.core.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

/**
 * Runs code only on the physical client.
 * The double supplier ({@code () -> () -> ...}) keeps client-only classes from loading on a server.
 */
public final class ClientOnly {
    private ClientOnly() {}

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static void run(Supplier<Runnable> code) {
        if (isClient())
            code.get().run();
    }
}
