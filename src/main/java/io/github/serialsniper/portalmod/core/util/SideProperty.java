package io.github.serialsniper.portalmod.core.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.state.IntegerProperty;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class SideProperty extends IntegerProperty {
//    private final ImmutableSet<Integer> values;

    protected SideProperty(String name, int min, int max) {
        super(name, min, max);

//        if(min < 0) {
//            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
//        } else if(max <= min) {
//            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
//        } else {
//            Set<Integer> set = Sets.newHashSet();
//
//            for(int i = min; i <= max; i++) {
////                int bitsOn = 0;
////
////                for(int j = 0; j < 4; j++)
////                    if((i & 0xF & (1 << j)) == 1)
////                        bitsOn++;
////
////                if(bitsOn != 3)
//                    set.add(i);
//            }
//
//            this.values = ImmutableSet.copyOf(set);
//        }
    }

    public static SideProperty create(String name, int min, int max) {
        return new SideProperty(name, min, max);
    }
}