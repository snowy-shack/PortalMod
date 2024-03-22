package net.portalmod.common.sorted.portalgun.api;

import java.util.ArrayList;
import java.util.UUID;

public class SkinDeserializer extends ArrayList<SkinDeserializer.Skin> {
    private static final long serialVersionUID = 1L;
    
    public static class Skin {
        public UUID id;
//        Timestamp last_edited;
        public String name;
        public boolean obtainable;
        public String obtaining_description;
    }
}