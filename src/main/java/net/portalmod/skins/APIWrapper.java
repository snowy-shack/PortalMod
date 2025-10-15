package net.portalmod.skins;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class APIWrapper {
    public static final String API_URL = "https://api.portalmod.net/v1";
    private static String BEARER_TOKEN;
    private static final Gson GSON = new Gson();

    private static final HttpClient httpClient = HttpClients.createDefault();
    private static boolean initialized = false;

    public static void init() throws IOException {
        HttpGet request = new HttpGet(API_URL + "/keys/bearer.txt");
        BEARER_TOKEN = makeRequest(request);
        initialized = true;
    }

    public static String makeRequest(HttpUriRequest request) throws IOException {
        HttpEntity response = httpClient.execute(request).getEntity();
        if(response == null)
            throw new IOException("Server didn't respond");
        InputStream istream = response.getContent();
        return IOUtils.toString(istream, StandardCharsets.UTF_8);
    }

    public static List<PortalGunSkin> getAllSkins() throws IOException {
        HttpGet request = new HttpGet(APIWrapper.API_URL + "/skins");

        String response = APIWrapper.makeRequest(request);
        return GSON.fromJson(response, PortalGunSkin.Deserializer.class);
    }

    public static Pair<Integer, List<String>> getUserData(String uuid) throws IOException {
        HttpGet request = new HttpGet(APIWrapper.API_URL + "/player/" + uuid);
        String response = APIWrapper.makeRequest(request);

        JsonObject unlocked = GSON.fromJson(response, JsonObject.class);

        JsonArray arr = unlocked.getAsJsonArray("skins");
        int def_color = unlocked.getAsJsonPrimitive("default_color").getAsInt();

        List<String> skinNames = arr == null
                ? Collections.emptyList()
                : StreamSupport.stream(arr.spliterator(), false)
                        .map(JsonElement::getAsString)
                        .collect(Collectors.toList());

        return new Pair<>(def_color, skinNames);
    }

    public static String getBearer() throws IOException {
        if(!initialized)
            init();
        return BEARER_TOKEN;
    }
}