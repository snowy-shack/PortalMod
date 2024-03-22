package net.portalmod.skins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.portalmod.PortalMod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class APIWrapper {
    public static final String API_URL = "https://api.portalmod.net";
    private static String BEARER_TOKEN;

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

    public static String getBearer() throws IOException {
        if(!initialized)
            init();
        return BEARER_TOKEN;
    }
}