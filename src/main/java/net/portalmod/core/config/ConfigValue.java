package net.portalmod.core.config;

/** One config entry. {@link #set} saves the whole config file. */
public final class ConfigValue<T> {
    final String key;
    final T defaultValue;
    T value;
    private final Runnable saver;

    ConfigValue(String key, T defaultValue, Runnable saver) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.saver = saver;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        saver.run();
    }
}
