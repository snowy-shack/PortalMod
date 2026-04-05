package net.portalmod.common.sorted.portalgun.skins;

import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.client.animation.PortalGunAnimatedTexture;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.PacketInit;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClientSkinManager extends SkinManager {
    private static ClientSkinManager instance;

    private ClientSkinManager() {
        super(true);
    }

    public static ClientSkinManager getInstance() {
        if(instance == null)
            instance = new ClientSkinManager();
        return instance;
    }

    @Override
    public File getModFolder() {
        return new File(Minecraft.getInstance().gameDirectory, "portalmod");
    }

    public ResourceLocation getSkinLocation(String id) {
        return new ResourceLocation(PortalMod.MODID, "portalgun/" + id);
    }

    public PortalGunAnimatedTexture getSkinTexture(String id) {
        return (PortalGunAnimatedTexture)Minecraft.getInstance().textureManager.getTexture(this.getSkinLocation(id));
    }

    @Override
    protected void uploadAllSkins(boolean overwrite) {
        this.getSkinCatalog().forEach((k, v) -> this.uploadSkin(v.skin_id, overwrite));
    }

    protected void uploadSkin(String id, boolean overwrite) {
        Texture currentTexture = Minecraft.getInstance().textureManager.getTexture(this.getSkinLocation(id));
        if(!(currentTexture instanceof PortalGunAnimatedTexture) || overwrite) {
            PortalGunAnimatedTexture skinTexture = new PortalGunAnimatedTexture(id, this.getSkinCatalog().get(id).framerate);
            Minecraft.getInstance().textureManager.register(this.getSkinLocation(id), skinTexture);
        }
    }

    public boolean hasUUID() {
        return !Minecraft.getInstance().getUser().getUuid().isEmpty();
    }

    @Override
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

    public void onClientStartup() {
        if(!this.clientSide)
            return;

        try {
            this.loadSkinCatalog();
            this.uploadAllSkins(true);
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
                this.fetchNewSkins();
                this.fetchPlayer(uuid);
                this.updateConfigHasSkinsFlag();
                this.uploadAllSkins(true);

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