package net.portalmod.common.sorted.turret;

public enum TurretState {
    RESTING,        // Default
    OPENING,        // Opening wings
    SHOOTING,       // Pew pew
    LOST_TARGET,    // Aiming laser back to the middle
    CLOSING,        // Closing wings
    FALLING,        // Falling over
    DEAD,           // Completely deactivated
}
