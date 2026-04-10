package net.portalmod.common.sorted.portalgun.skins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.math.MathHelper;
import net.portalmod.PortalMod;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.DataUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public abstract class SkinManager {
    protected final boolean clientSide;
    protected final Gson gson;
    protected final Map<String, PortalGunSkin> skinCatalog;
    protected final Map<UUID, PortalGunPlayer> players;
    protected final Map<UUID, String> selectedSkinPerPlayer;
    protected final Map<UUID, Integer> tintPerPlayer;
    protected final Deque<Runnable> pendingTasks;
    protected final Deque<Runnable> pendingCallbacks;
    protected Thread taskThread;
    protected volatile boolean callbackPending;

    protected SkinManager(boolean clientSide) {
        this.clientSide = clientSide;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.skinCatalog = Collections.synchronizedMap(new LinkedHashMap<>());
        this.players = new ConcurrentHashMap<>();
        this.selectedSkinPerPlayer = new ConcurrentHashMap<>();
        this.tintPerPlayer = new ConcurrentHashMap<>();
        this.pendingTasks = new ConcurrentLinkedDeque<>();
        this.pendingCallbacks = new ArrayDeque<>();
    }

    // CONSTANT METHODS

    public abstract File getModFolder();

    public File getCacheFolder() {
        return new File(this.getModFolder(), "cache");
    }

    public File getSkinsFolder() {
        return new File(this.getCacheFolder(), "skins");
    }

    public File getPlayersFolder() {
        return new File(this.getCacheFolder(), "players");
    }

    protected PortalGunSkin getDefaultSkin() {
        PortalGunSkin defaultSkin = new PortalGunSkin();
        defaultSkin.skin_id = "default";
        defaultSkin.name = "Default";
        defaultSkin.description = "The classic look!";
        return defaultSkin;
    }

    // PLAYER

    protected void fetchPlayer(UUID player) throws IOException {
        String data;
        try {
            data = DataUtil.makeTextRequest("https://api.portalmod.net/v1/player/" + player);
        } catch(IOException e) {
            throw new IOException("Failed to fetch player: " + player);
        }

        PortalGunPlayer payload = this.gson.fromJson(data, PortalGunPlayer.class);
        this.fetchTheseNewSkins(new HashSet<>(payload.skins));

        if(player != null)
            this.players.put(player, payload);
    }

    protected void cachePlayer(UUID player) throws IOException {
        File playerCache = DataUtil.tryCreateFolderAndGetFile(this.getPlayersFolder(), player + ".json");

        try {
            DataUtil.writeTextFile(playerCache, gson.toJson(this.getPlayerSkins(player)));
        } catch(IOException e) {
            throw new IOException("Failed to cache player: " + player);
        }
    }

    protected void loadPlayer(UUID player) throws IOException {
        File playerCache = DataUtil.tryCreateFolderAndGetFile(this.getPlayersFolder(), player + ".json");

        if(!playerCache.exists())
            return;

        String data;
        try {
            data = DataUtil.loadTextFile(playerCache);
        } catch(IOException e) {
            throw new IOException("Failed to load player: " + player);
        }

        ArrayList<String> skins = this.gson.fromJson(data, new TypeToken<ArrayList<String>>(){}.getType());
        if(player != null)
            this.players.put(player, new PortalGunPlayer(skins));
    }

    // SKIN CATALOG

    protected void fetchSkinCatalog() throws IOException {
        String data;
        try {
            data = DataUtil.makeTextRequest("https://api.portalmod.net/v1/skins");
        } catch(IOException e) {
            throw new IOException("Failed to fetch skin catalog");
        }

        this.populateSkinCatalog(data);
    }

    protected void cacheSkinCatalog() throws IOException {
        File skinCatalog = DataUtil.tryCreateFolderAndGetFile(this.getSkinsFolder(), "skin_catalog.json");

        try {
            DataUtil.writeTextFile(skinCatalog, gson.toJson(this.skinCatalog.values()));
        } catch(IOException e) {
            throw new IOException("Failed to cache skin catalog");
        }
    }

    protected void loadSkinCatalog() throws IOException {
        File skinCatalog = DataUtil.tryCreateFolderAndGetFile(this.getSkinsFolder(), "skin_catalog.json");

        if(!skinCatalog.exists())
            return;

        String data;
        try {
            data = DataUtil.loadTextFile(skinCatalog);
        } catch(IOException e) {
            throw new IOException("Failed to load skin catalog");
        }

        this.populateSkinCatalog(data);
    }

    protected void populateSkinCatalog(String json) {
        LinkedHashMap<String, PortalGunSkin> map = this.gson.fromJson(json, PortalGunSkin.Deserializer.class).stream()
                .collect(Collectors.toMap(
                        skin -> skin.skin_id,
                        skin -> skin,
                        (o, n) -> n,
                        LinkedHashMap::new
                ));

        this.skinCatalog.clear();
        this.skinCatalog.putAll(map);
    }

    // SKINS

    protected void uploadAllSkins(boolean overwrite) {}

    protected void updateSkin(PortalGunSkin skin, File skinFile) throws IOException {
        if(this.isSkinUpToDate(skin, skinFile) || skin.checksum == null)
            return;

        byte[] imageData;

        try {
            imageData = DataUtil.makeRequest("https://cdn.portalmod.net/skins/" + skin.skin_id + ".png");
            if(!this.isSkinChecksumCorrect(skin, imageData))
                throw new IOException();
        } catch(IOException e) {
            throw new IOException("Failed to download skin: " + skin.skin_id);
        }

        try {
            DataUtil.writeFile(skinFile, imageData);
        } catch(IOException e) {
            throw new IOException("Failed to cache skin: " + skin.skin_id);
        }
    }

    protected void updateAllSkins() throws IOException {
        File skinsFolder = DataUtil.tryCreateFolder(new File(this.getSkinsFolder(), "textures"));

        for(PortalGunSkin skin : this.getSkinCatalog().values()) {
            if(!skin.skin_id.equals("default")) {
                try {
                    this.updateSkin(skin, new File(skinsFolder, skin.skin_id + ".png"));
                } catch(IOException e) {
                    PortalMod.LOGGER.error(e.getMessage());
                }
            }
        }
    }

    protected byte[] loadSkin(File skinFile) throws IOException {
        return DataUtil.loadFile(skinFile);
    }

    protected boolean isSkinChecksumCorrect(PortalGunSkin skin, byte[] data) throws IOException {
        return DataUtil.computeChecksum(data).equals(skin.checksum);
    }

    protected boolean isSkinUpToDate(PortalGunSkin skin, File skinFile) throws IOException {
        if(!skinFile.exists())
            return false;

        try {
            return DataUtil.computeChecksum(this.loadSkin(skinFile)).equals(skin.checksum);
        } catch(IOException e) {
            throw new IOException("Failed to load skin: " + skin.skin_id);
        }
    }

    protected void fetchNewSkins() {
        try {
            this.fetchSkinCatalog();
            this.cacheSkinCatalog();

            if(this.clientSide) {
                this.updateAllSkins();
            }
        } catch(IOException e) {
            PortalMod.LOGGER.error(e.getMessage());
        }
    }

    protected void fetchTheseNewSkins(Set<String> skins) {
        List<String> newSkins = skins.stream()
                .filter(newSkin -> !this.getSkinCatalog().containsKey(newSkin))
                .collect(Collectors.toList());

        if(!newSkins.isEmpty()) {
            this.fetchNewSkins();
        }
    }

    // INTERFACES

    public Map<String, PortalGunSkin> getSkinCatalog() {
        Map<String, PortalGunSkin> skins = new LinkedHashMap<>(this.skinCatalog);
        skins.putIfAbsent("default", this.getDefaultSkin());
        return skins;
    }

    public PortalGunSkin getSkinDefinition(String id) {
        return this.getSkinCatalog().get(id);
    }

    public boolean isSkinTintable(String skin) {
        return this.getSkinDefinition(skin).tintable;
    }

    public PortalGunPlayer getPlayer(UUID player) {
        if(this.clientSide) {
            if(player == null && this.getOwnUUID().isPresent())
                player = this.getOwnUUID().get();
        }

        if(player == null)
            return new PortalGunPlayer();
        return this.players.getOrDefault(player, new PortalGunPlayer());
    }

    public Set<String> getPlayerSkins(UUID player) {
        return new HashSet<>(this.getPlayer(player).skins);
    }

    public boolean playerHasSkin(UUID player, String skin) {
        Set<String> skins = this.getPlayerSkins(player);
        skins.add("default");
        return skins.contains(skin);
    }

    public String getSelectedSkinForPlayer(UUID player) {
        if(this.clientSide) {
            if(player == null && this.getOwnUUID().isPresent())
                player = this.getOwnUUID().get();
        }

        if(player == null)
            return "default";
        return this.selectedSkinPerPlayer.getOrDefault(player, "default");
    }

    public void setSelectedSkinForPlayer(UUID player, String skin) {
        if(this.clientSide) {
            if(player == null && this.getOwnUUID().isPresent())
                player = this.getOwnUUID().get();
            this.fetchTheseNewSkins(Collections.singleton(skin));
        }

        if(player != null)
            this.selectedSkinPerPlayer.put(player, skin);
    }

    public int getTintForPlayer(UUID player) {
        if(this.clientSide) {
            if(player == null && this.getOwnUUID().isPresent())
                player = this.getOwnUUID().get();
        }

        if(player == null)
            return 0;
        return this.tintPerPlayer.getOrDefault(player, 0);
    }

    public int getTintForPlayerOnSkin(UUID player, String skin) {
        return this.isSkinTintable(skin) ? this.getTintForPlayer(player) : 0;
    }

    public void setTintForPlayer(UUID player, int tint) {
        if(this.clientSide) {
            if(player == null && this.getOwnUUID().isPresent())
                player = this.getOwnUUID().get();
        }

        if(player != null)
            this.tintPerPlayer.put(player, tint);
    }

    public void setSkinAndTintForPlayer(UUID player, String skin, int tint) {
        this.setSelectedSkinForPlayer(player, skin);
        if(tint != 0) {
            this.setTintForPlayer(player, tint);
        }
    }

    public Map<UUID, String> getSelectedSkinMap() {
        return this.selectedSkinPerPlayer;
    }

    public void clearSelectedSkinMapExceptSelf() {
        if(!this.getOwnUUID().isPresent()) {
            this.selectedSkinPerPlayer.clear();
            return;
        }

        this.selectedSkinPerPlayer.entrySet().removeIf(player -> !player.getKey().equals(this.getOwnUUID().get()));
    }

    public void clearTintMapExceptSelf() {
        if(!this.getOwnUUID().isPresent()) {
            this.tintPerPlayer.clear();
            return;
        }

        this.tintPerPlayer.entrySet().removeIf(player -> !player.getKey().equals(this.getOwnUUID().get()));
    }

    // TASKS

    public void enqueueTask(Runnable task) {
        this.pendingTasks.add(task);
        this.startThreadIfNeeded();
    }

    public void enqueueCallback(Runnable task) {
        this.pendingCallbacks.add(task);
    }

    protected void startThreadIfNeeded() {
        if(this.taskThread == null && !this.pendingTasks.isEmpty()) {
            this.taskThread = new Thread(this::executeTasks);
            this.taskThread.start();
        }
    }

    protected synchronized void executeTasks() {
        while(!this.pendingTasks.isEmpty()) {
            Runnable task = this.pendingTasks.poll();
            if(task != null)
                task.run();
        }

        this.callbackPending = true;
    }

    public void tick() {
        if(!this.callbackPending) {
            this.startThreadIfNeeded();
            return;
        }

        this.callbackPending = false;

        if(this.clientSide) {
            this.uploadAllSkins(false);
        }

        while(!this.pendingCallbacks.isEmpty()) {
            Runnable callback = this.pendingCallbacks.poll();
            if(callback != null)
                callback.run();
        }

        this.taskThread = null;
    }

    // UTIL

    public void updateConfigHasSkinsFlag() {
        PortalModConfigManager.HAS_SKINS.set(!this.getPlayerSkins(null).isEmpty());
    }

    public void setConfigSelectedSkin(String skin) {
        PortalModConfigManager.PORTALGUN_SKIN.set(skin);
    }

    public String adjustSelectedSkin(String skin) {
        return this.playerHasSkin(null, skin) ? skin : "default";
    }

    public void applyConfigDefaultTint() {
        PortalGunPlayer payload = this.getPlayer(null);
        if(payload.default_color != 0)
            this.setConfigTint(payload.default_color);
    }

    public void setConfigTint(int tint) {
        PortalModConfigManager.SKIN_TINT.set(tint);
    }

    public int adjustTint(int tint) {
        Vec3 hsv = tint != 0 ? new Colour(tint).getHSV() : new Vec3(0, 0, 1);
        float valueMin = ColorPickerWidget.SV_PICKER_VAL_MIN;
        float valueMax = ColorPickerWidget.SV_PICKER_VAL_MAX;

        hsv.x = MathHelper.clamp(hsv.x, 0, 360);
        hsv.y = MathHelper.clamp(hsv.y, 0, 1);
        hsv.z = MathHelper.clamp(hsv.z, valueMin, valueMax);
        return Colour.fromHSV((float)hsv.x, (float)hsv.y, (float)hsv.z).getRGBValue();
    }

    public Optional<UUID> getOwnUUID() {
        return Optional.empty();
    }
}