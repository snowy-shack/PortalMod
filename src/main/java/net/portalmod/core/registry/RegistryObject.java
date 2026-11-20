package net.portalmod.core.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Holds an object that was registered into a vanilla registry.
 * Fabric registers eagerly, so the value is available immediately.
 */
public final class RegistryObject<T> implements Supplier<T> {
    private final ResourceLocation id;
    private final T value;

    RegistryObject(ResourceLocation id, T value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    public ResourceLocation getId() {
        return id;
    }
}
