package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class EntityLoopableSound extends EntityTickableSound {
    public EntityLoopableSound(Entity entity, SoundEvent sound, SoundCategory category) {
        super(sound, category, entity);
        this.looping = true;
    }
}
