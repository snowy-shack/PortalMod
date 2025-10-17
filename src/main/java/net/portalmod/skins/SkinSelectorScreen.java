package net.portalmod.skins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.client.animation.AnimatedTexture;
import net.portalmod.common.sorted.portalgun.PortalGunISTER;
import net.portalmod.common.sorted.portalgun.api.SkinLoader;
import net.portalmod.core.init.ItemInit;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.portalmod.PortalMod;

public class SkinSelectorScreen extends Screen implements IProgressMeter {
    private final Screen lastScreen;

    public SkinSelectorScreen(Screen lastScreen) {
        super(new TranslationTextComponent("options." + PortalMod.MODID + ".skins.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        new Thread().start();
    }

//    private void downloadData() {
//        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").create();
//
//        try {
//            List<PortalGunSkin> skins = ClientSkinConfig.getSkins();
////
////
////            HttpClient httpClient = HttpClients.createDefault();
////            HttpPost request = new HttpPost("https://api.portalmod.net/skins");
////            request.addHeader("Authorization", "Bearer " + PortalMod.API_BEARER);
////
////            HttpResponse response = httpClient.execute(request);
////            HttpEntity entity = response.getEntity();
//
////            if(entity != null) {
////                try(InputStream instream = entity.getContent()) {
////                    String s = IOUtils.toString(instream, StandardCharsets.UTF_8);
////                    System.out.println(s);
////                    SkinDeserializer skins = gson.fromJson(s, SkinDeserializer.class);
//
//                    for(PortalGunSkin skin : skins) {
//                        AnimatedTexture tex = SkinLoader.loadSkin(skin.skin_id);
//
//                        if(response2.getStatusLine().getStatusCode() == 200) {
//                            File gameFolder = Minecraft.getInstance().gameDirectory;
//                            File modFolder = new File(gameFolder.getAbsolutePath() + PortalMod.MODID);
//                            File texture = new File(modFolder.getAbsolutePath() + "/" + skin.id + ".png");
//                            FileOutputStream ostream = new FileOutputStream(texture);
//                            InputStream istream = entity2.getContent();
//
////                            URL website = new URL("http://www.website.com/information.asp");
////                            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
////                            FileOutputStream fos = new FileOutputStream("information.html");
//                            ReadableByteChannel rbc = Channels.newChannel(istream);
//                            ostream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//
//                            while(istream.available() > 0)
//                                ostream.write(istream.read());
//                            ostream.close();
//                        }
//                    }
//                }
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//
////        try {
////            HttpClient httpClient = HttpClients.createDefault();
////            HttpPost request = new HttpPost("https://api.portalmod.net/players");
////            request.addHeader("Authorization", "Bearer " + PortalMod.API_BEARER);
////
////            JsonArray uuid = new JsonArray();
////            uuid.add(Minecraft.getInstance().player.getStringUUID());
////
////            JsonObject obj = new JsonObject();
////            obj.add("uuid", uuid);
////
////            StringEntity requestBody = new StringEntity(gson.toJson(obj));
////            request.setEntity(requestBody);
////
////            HttpResponse response = httpClient.execute(request);
////            HttpEntity entity = response.getEntity();
////
////            if(entity != null) {
////                try(InputStream instream = entity.getContent()) {
////                    String s = IOUtils.toString(instream, StandardCharsets.UTF_8);
////                    System.out.println(s);
////                    gson.fromJson(s, SkinDeserializer.class);
////                }
////            }
////        } catch(Exception e) {
////            e.printStackTrace();
////        }
//    }

    @Override
    public void onStatsUpdated() {

    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//       if(this.isLoading) {
        this.renderBackground(matrixStack);
//           drawCenteredString(p_230430_1_, this.font, PENDING_TEXT, this.width / 2, this.height / 2, 16777215);
        drawCenteredString(matrixStack, this.font, LOADING_SYMBOLS[(int) (Util.getMillis() / 150L % (long) LOADING_SYMBOLS.length)], this.width / 2, this.height / 2 + 9 * 2, 16777215);
//       } else {
//           this.getActiveList().render(matrixStack, mouseX, mouseY, partialTicks);
           drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);
           super.render(matrixStack, mouseX, mouseY, partialTicks);
//       }

//        MatrixStack matrix = new MatrixStack();
//        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//
//        matrixStack.pushPose();
//        matrixStack.translate(100, 100, 200); // screen position
//        matrixStack.scale(150f, 150f, 150f);    // scale model
//
//        ItemStack gun = new ItemStack(ItemInit.PORTALGUN.get());
//
//        Minecraft.getInstance().getItemRenderer().renderStatic(
//                gun,
//                ItemCameraTransforms.TransformType.GUI,
//                LightTexture.sky(255),
//                OverlayTexture.NO_OVERLAY,
//                matrixStack,
//                buffer
//        );
//
//        matrixStack.popPose();
//
//        buffer.endBatch();
    }

    @Override
    public void onClose() {
        close(false);
    }

    private void close(boolean goBackElseClose) {
        this.minecraft.setScreen(goBackElseClose || Minecraft.getInstance().level == null ? lastScreen : null);
    }
}