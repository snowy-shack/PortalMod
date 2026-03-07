package net.portalmod.common.sorted.portalgun.skins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.PortalMod;
import net.portalmod.client.animation.PortalGunAnimatedTexture;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.DataUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class SkinManager {
    private static SkinManager clientInstance;
    private static SkinManager serverInstance;

    private final boolean clientSide;
    private final Gson gson;
    private final Map<String, PortalGunSkin> skinCatalog;
    private final Map<UUID, PortalGunPlayer> players;
    private final Map<UUID, String> selectedSkinPerPlayer;
    private final Map<UUID, Integer> tintPerPlayer;
    private final Deque<Runnable> pendingTasks;
    private final Deque<Runnable> pendingCallbacks;
    private Thread taskThread;
    private volatile boolean callbackPending;

    private SkinManager(boolean clientSide) {
        this.clientSide = clientSide;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.skinCatalog = Collections.synchronizedMap(new LinkedHashMap<>());
        this.players = new ConcurrentHashMap<>();
        this.selectedSkinPerPlayer = new ConcurrentHashMap<>();
        this.tintPerPlayer = new ConcurrentHashMap<>();
        this.pendingTasks = new ConcurrentLinkedDeque<>();
        this.pendingCallbacks = new ArrayDeque<>();
    }

    public static SkinManager getClientInstance() {
        if(clientInstance == null)
            clientInstance = new SkinManager(true);
        return clientInstance;
    }

    public static SkinManager getServerInstance() {
        if(serverInstance == null)
            serverInstance = new SkinManager(false);
        return serverInstance;
    }

    // CONSTANT METHODS

    public File getCacheFolder() {
        return new File(PortalMod.getModFolder(), "cache");
    }

    public File getSkinsFolder() {
        return new File(this.getCacheFolder(), "skins");
    }

    public File getPlayersFolder() {
        return new File(this.getCacheFolder(), "players");
    }

    public ResourceLocation getSkinLocation(String id) {
        return new ResourceLocation(PortalMod.MODID, "portalgun/" + id);
    }

    public PortalGunAnimatedTexture getSkinTexture(String id) {
        return (PortalGunAnimatedTexture)Minecraft.getInstance().textureManager.getTexture(this.getSkinLocation(id));
    }

    private PortalGunSkin getDefaultSkin() {
        PortalGunSkin defaultSkin = new PortalGunSkin();
        defaultSkin.skin_id = "default";
        defaultSkin.name = "Default";
        defaultSkin.description = "The classic look!";
        return defaultSkin;
    }

    // PLAYER

    private void fetchPlayer(UUID player) throws IOException {
        String data;
        try {
            data = DataUtil.makeTextRequest("https://api.portalmod.net/v1/player/" + player);
        } catch(IOException e) {
            throw new IOException("Failed to fetch player: " + player);
        }

        PortalGunPlayer payload = this.gson.fromJson(data, PortalGunPlayer.class);
        if(this.clientSide)
            this.fetchNewSkins(new HashSet<>(payload.skins));

        if(player != null)
            this.players.put(player, payload);
    }

    private void cachePlayer(UUID player) throws IOException {
        File playerCache = DataUtil.tryCreateFolderAndGetFile(this.getPlayersFolder(), player + ".json");

        try {
            DataUtil.writeTextFile(playerCache, gson.toJson(this.getPlayerSkins(player)));
        } catch(IOException e) {
            throw new IOException("Failed to cache player: " + player);
        }
    }

    private void loadPlayer(UUID player) throws IOException {
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

    private void fetchSkinCatalog() throws IOException {
        String data;
        try {
            data = DataUtil.makeTextRequest("https://api.portalmod.net/v1/skins");
        } catch(IOException e) {
            throw new IOException("Failed to fetch skin catalog");
        }

        this.populateSkinCatalog(data);
    }

    private void cacheSkinCatalog() throws IOException {
        File skinCatalog = DataUtil.tryCreateFolderAndGetFile(this.getSkinsFolder(), "skin_catalog.json");

        try {
            DataUtil.writeTextFile(skinCatalog, gson.toJson(this.skinCatalog.values()));
        } catch(IOException e) {
            throw new IOException("Failed to cache skin catalog");
        }
    }

    private void loadSkinCatalog() throws IOException {
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

    private void populateSkinCatalog(String json) {
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

    private void uploadSkin(String id) {
        Texture currentTexture = Minecraft.getInstance().textureManager.getTexture(this.getSkinLocation(id));
        if(!(currentTexture instanceof PortalGunAnimatedTexture)) {
            PortalGunAnimatedTexture skinTexture = new PortalGunAnimatedTexture(id, this.getSkinCatalog().get(id).framerate);
            Minecraft.getInstance().textureManager.register(this.getSkinLocation(id), skinTexture);
        }
    }

    private void uploadAllSkins() {
        this.getSkinCatalog().forEach((k, v) -> this.uploadSkin(v.skin_id));
    }

    private void updateSkin(PortalGunSkin skin, File skinFile) throws IOException {
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

    private void updateAllSkins() throws IOException {
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

    private byte[] loadSkin(File skinFile) throws IOException {
        return DataUtil.loadFile(skinFile);
    }

    private boolean isSkinChecksumCorrect(PortalGunSkin skin, byte[] data) throws IOException {
        return DataUtil.computeChecksum(data).equals(skin.checksum);
    }

    private boolean isSkinUpToDate(PortalGunSkin skin, File skinFile) throws IOException {
        if(!skinFile.exists())
            return false;

        try {
            return DataUtil.computeChecksum(this.loadSkin(skinFile)).equals(skin.checksum);
        } catch(IOException e) {
            throw new IOException("Failed to load skin: " + skin.skin_id);
        }
    }

    private void fetchNewSkins(Set<String> skins) {
        List<String> newSkins = skins.stream()
                .filter(newSkin -> !this.getSkinCatalog().containsKey(newSkin))
                .collect(Collectors.toList());

        if(!newSkins.isEmpty()) {
            try {
                this.fetchSkinCatalog();
                this.cacheSkinCatalog();
                this.updateAllSkins();
            } catch(IOException e) {
                PortalMod.LOGGER.error(e.getMessage());
            }
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
            this.fetchNewSkins(Collections.singleton(skin));
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

    private void startThreadIfNeeded() {
        if(this.taskThread == null && !this.pendingTasks.isEmpty()) {
            this.taskThread = new Thread(this::executeTasks);
            this.taskThread.start();
        }
    }

    private synchronized void executeTasks() {
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
            this.uploadAllSkins();
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

    public boolean hasUUID() {
        return !Minecraft.getInstance().getUser().getUuid().isEmpty();
    }

    public Optional<UUID> getOwnUUID() {
        String playerUUIDString = Minecraft.getInstance().getUser().getUuid();

        UUID playerUUID;
        try {
            playerUUID = UUIDTypeAdapter.fromString(playerUUIDString);
        } catch(IllegalArgumentException e) {
            PortalMod.LOGGER.error("Failed to get own UUID");
            return Optional.empty();
        }

        return Optional.of(playerUUID);
    }

    // PROCEDURES

    public void onClientStartup() {
        if(!this.clientSide)
            return;

        try {
            this.loadSkinCatalog();
            this.uploadAllSkins();
        } catch(IOException e) {
            PortalMod.LOGGER.error(e.getMessage());
        }

        String selectedSkin = PortalModConfigManager.PORTALGUN_SKIN.get();
        int tint = PortalModConfigManager.SKIN_TINT.get();

        if(selectedSkin.equals("default") && !PortalModConfigManager.HAS_SKINS.get())
            return;

        Optional<UUID> optionalUUID = this.getOwnUUID();
        if(optionalUUID.isPresent()) {
            UUID uuid = optionalUUID.get();

            try {
                this.fetchPlayer(uuid);
                this.updateConfigHasSkinsFlag();
                this.uploadAllSkins();

                selectedSkin = this.adjustSelectedSkin(selectedSkin);
                this.setConfigSelectedSkin(selectedSkin);
                this.setSelectedSkinForPlayer(null, selectedSkin);

                if(this.getSkinDefinition(selectedSkin).tintable) {
                    tint = this.adjustTint(tint);
                    this.setTintForPlayer(uuid, tint);
                }

            } catch(IOException e) {
                PortalMod.LOGGER.error(e.getMessage());
            }
        }
    }

    public void onClientLogin() {
        if(!this.clientSide)
            return;

        this.clearSelectedSkinMapExceptSelf();
        this.clearTintMapExceptSelf();
        String selectedSkin = this.getSelectedSkinForPlayer(null);
        int tint = this.getTintForPlayerOnSkin(null, selectedSkin);
        PacketInit.INSTANCE.sendToServer(new CSetPlayerSkinPacket(selectedSkin, tint));
    }

    public void onServerLogin(ServerPlayerEntity player) {
        if(this.clientSide)
            return;

        this.enqueueTask(() -> {
            Map<UUID, String> temp = new HashMap<>(this.getSelectedSkinMap());
            temp.forEach((uuid, skin) -> {
                if(!uuid.equals(player.getUUID())) {
                    int tint = this.getTintForPlayerOnSkin(uuid, skin);
                    PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            new SSetPlayerSkinPacket(uuid, skin, tint));
                }
            });
        });
    }

    public void onClientReceivedPacket(SSetPlayerSkinPacket packet) {
        if(!this.clientSide)
            return;

        this.enqueueTask(() -> CompletableFuture.runAsync(() -> {
            UUID uuid = packet.getPlayer();
            String skin = packet.getSkin();
            int tint = this.isSkinTintable(skin) ? packet.getTint() : 0;

            this.setSkinAndTintForPlayer(uuid, skin, tint);
        }));
    }

    public void onServerReceivedPacket(ServerPlayerEntity player, CSetPlayerSkinPacket packet) {
        if(this.clientSide)
            return;

        this.enqueueTask(() -> {
            UUID uuid = player.getUUID();
            String skin = packet.getSkin();
            int tint = packet.getTint();

            try {
                this.loadPlayer(uuid);
            } catch(IOException e) {
                PortalMod.LOGGER.error(e.getMessage());
            }

            if(!this.playerHasSkin(uuid, skin)) {
                try {
                    this.fetchPlayer(uuid);
                    this.cachePlayer(uuid);

                    if(!this.playerHasSkin(uuid, skin))
                        return;

                } catch(IOException e) {
                    PortalMod.LOGGER.error(e.getMessage());
                    return;
                }
            }

            this.setSkinAndTintForPlayer(uuid, skin, tint);

            PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SSetPlayerSkinPacket(uuid, skin, tint));
        });
    }

    public void onSkinCatalogRefresh() {
        if(!this.clientSide)
            return;

        this.enqueueTask(() -> {
            Optional<UUID> ownUUID = this.getOwnUUID();

            if(ownUUID.isPresent()) {
                try {
                    this.fetchPlayer(ownUUID.get());
                    if(!PortalModConfigManager.HAS_SKINS.get())
                        this.applyConfigDefaultTint();
                    this.updateConfigHasSkinsFlag();

                } catch(IOException e) {
                    PortalMod.LOGGER.error(e.getMessage());
                }
            }

            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                return;
            }
        });
    }

    public void onSkinSelected(String skin, int tint) {
        if(!this.clientSide)
            return;

        tint = this.isSkinTintable(skin) ? tint : 0;
        this.setSkinAndTintForPlayer(null, skin, tint);

        this.setConfigSelectedSkin(skin);
        if(this.isSkinTintable(skin)) {
            this.setConfigTint(tint);
        }

        if(Minecraft.getInstance().getConnection() != null) {
            PacketInit.INSTANCE.sendToServer(new CSetPlayerSkinPacket(skin, tint));
        }
    }
}