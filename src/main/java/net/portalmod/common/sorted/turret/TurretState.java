package net.portalmod.common.sorted.turret;
import net.minecraft.util.IStringSerializable;

public enum TurretState implements IStringSerializable {
    RESTING,        // Default
    OPENING,        // Opening wings
    SHOOTING,       // Pew pew
    LOST_TARGET,    // Aiming laser back to the middle
    CLOSING,        // Closing wings
    FALLING,        // Falling over
    DEAD,           // Completely deactivated
    ;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
