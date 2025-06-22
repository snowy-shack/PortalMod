package net.portalmod.core.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.portalmod.PortalMod;

public class TextureInit {
    private static final Registry BLOCKS = new Registry(AtlasTexture.LOCATION_BLOCKS);
    
    private TextureInit() {}
    
    public static final ResourceLocation FAITHPLATE_TARGET = BLOCKS.register("block/faithplate_target");
    
    public static void register(TextureStitchEvent.Pre event) {
        Registry.register(event);
    }
    
    private static class Registry {
        private static final Map<ResourceLocation, Registry> REGISTRIES = new HashMap<>();
        private final List<ResourceLocation> ENTRIES = new ArrayList<>();
        
        public Registry(ResourceLocation phase) {
            REGISTRIES.put(phase, this);
        }
        
        public ResourceLocation register(String name) {
            ResourceLocation location = new ResourceLocation(PortalMod.MODID, name);
            ENTRIES.add(location);
            return location;
        }
        
        public static void register(TextureStitchEvent.Pre event) {
            ResourceLocation phase = event.getMap().location();
            if(REGISTRIES.containsKey(phase))
                for(ResourceLocation entry : REGISTRIES.get(phase).ENTRIES)
                    event.addSprite(entry);
        }
    }
}