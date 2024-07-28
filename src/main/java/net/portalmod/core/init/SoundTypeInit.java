package net.portalmod.core.init;

import net.minecraft.block.SoundType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

import java.util.function.Supplier;

public class SoundTypeInit {
    private SoundTypeInit() {}
    
    public static final Entry GEL = new Entry(1, 1,
            () -> SoundInit.GEL_BREAK.get(),
            () -> SoundInit.GEL_STEP.get(),
            () -> SoundInit.GEL_PLACE.get(),
            () -> SoundEvents.SLIME_BLOCK_HIT, // whata theeeese
            () -> SoundEvents.SLIME_BLOCK_FALL);
    
    public static class Entry extends SoundType {
        private final Supplier<SoundEvent> breakSound;
        private final Supplier<SoundEvent> stepSound;
        private final Supplier<SoundEvent> placeSound;
        private final Supplier<SoundEvent> hitSound;
        private final Supplier<SoundEvent> fallSound;
        
        public Entry(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
                Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
            super(volume, pitch, null, null, null, null, null);
            this.breakSound = breakSound;
            this.stepSound = stepSound;
            this.placeSound = placeSound;
            this.hitSound = hitSound;
            this.fallSound = fallSound;
        }
        
        @Override
        public SoundEvent getBreakSound() {
            return breakSound.get();
        }
        
        @Override
        public SoundEvent getStepSound() {
            return stepSound.get();
        }
        
        @Override
        public SoundEvent getPlaceSound() {
            return placeSound.get();
        }
        
        @Override
        public SoundEvent getHitSound() {
            return hitSound.get();
        }
        
        @Override
        public SoundEvent getFallSound() {
            return fallSound.get();
        }
    }
}