package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public class EntityLoopableSound extends EntityBoundSoundInstance {
    public EntityLoopableSound(Entity entity, SoundEvent sound, SoundSource category) {
        super(sound, category, entity);
        this.looping = true;
    }
}
