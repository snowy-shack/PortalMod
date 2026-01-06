package net.portalmod.skins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.portalmod.core.util.Registry;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkinManager {
    private static final Gson gson = new GsonBuilder().create();
    private static final Map<UUID, PortalGunSkin> REGISTRY = new HashMap<>();
    private static boolean initialized = false;

    public static void init() {
//        try {
//            HttpPost request = new HttpPost(APIWrapper.API_URL + "/skins");
//            request.addHeader("Authorization", "Bearer " + APIWrapper.getBearer());
//            List<PortalGunSkin> skins = gson.fromJson(APIWrapper.makeRequest(request), PortalGunSkin.Deserializer.class);
//
//            for(PortalGunSkin skin : skins)
//                REGISTRY.put(skin.id, skin);
//            initialized = true;
//        } catch(IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static PortalGunSkin getSkin(UUID id) {
        return REGISTRY.get(id);
    }
}