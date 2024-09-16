package net.portalmod.common.sorted.turret;

public enum TurretState {
    RESTING,        // Default
    TARGETING,      // Aiming laser towards target
    OPENING,        // Opening wings
    SHOOTING,       // Pew pew
    LOST_TARGET,    // Aiming laser back to the middle
    CLOSING,        // Closing wings
    KILLED          // Fallen over
}
