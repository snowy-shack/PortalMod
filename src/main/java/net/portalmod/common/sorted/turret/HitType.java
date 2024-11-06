package net.portalmod.common.sorted.turret;
import net.minecraft.util.IStringSerializable;

public enum HitType implements IStringSerializable {
    CLEAR,       // No block hits
    PERMUABLE,   // Went through permuable blocks
    TRANSPARENT, // Hit a solid but transparent block
    SOLID,       // Hit a solid wall
    CUBE,        // Hits a cube
    ;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
