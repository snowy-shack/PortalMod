package net.portalmod.common.sorted.turret;
import net.minecraft.util.StringRepresentable;

public enum HitType implements StringRepresentable {
    CLEAR,       // No block hits
    PERMEABLE,   // Went through permeable blocks
    TRANSPARENT, // Hit a solid but transparent block
    SOLID,       // Hit a solid wall
    CUBE,        // Hits a cube
    ;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
