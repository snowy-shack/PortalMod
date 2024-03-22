package net.portalmod.core.math;

import java.util.HashMap;

public class BiHashMap<K1, K2, V> {
    private final HashMap<K1, HashMap<K2, V>> MAP = new HashMap<>();
    
    public V get(K1 k1, K2 k2) {
        HashMap<K2, V> map1 = new HashMap<>();
        
        if(MAP.containsKey(k1))
            map1 = MAP.get(k1);
        
        return map1.get(k2);
    }
    
    public void put(K1 k1, K2 k2, V value) {
        HashMap<K2, V> map1 = new HashMap<>();
        
        if(MAP.containsKey(k1))
            map1 = MAP.get(k1);
        
        map1.put(k2, value);
        MAP.put(k1, map1);
    }
}