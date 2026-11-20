package net.portalmod.core.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Small stand-in for Forge's DeferredRegister.
 * On Fabric, registering straight into the vanilla registry during mod init is the normal
 * approach, so {@link #register(String, Supplier)} registers immediately.
 */
public final class DeferredRegister<T> {
    private final Registry<T> registry;
    private final String modId;
    private final List<RegistryObject<? extends T>> entries = new ArrayList<>();

    private DeferredRegister(Registry<T> registry, String modId) {
        this.registry = registry;
        this.modId = modId;
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modId) {
        return new DeferredRegister<>(registry, modId);
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        ResourceLocation id = new ResourceLocation(modId, name);
        I value = Registry.register(registry, id, supplier.get());
        RegistryObject<I> obj = new RegistryObject<>(id, value);
        entries.add(obj);
        return obj;
    }

    /** Kept for call-site compatibility; loading the owning class already registered everything. */
    public void register() {}

    public Collection<RegistryObject<? extends T>> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
