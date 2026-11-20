package net.portalmod.core.injectors;

import java.util.List;

import net.portalmod.PortalMod;
import net.portalmod.mixins.accessors.LogoRendererAccessor;
import net.portalmod.mixins.accessors.MainMenuScreenAccessor;
import net.portalmod.mixins.accessors.MinecraftAccessor;
import net.portalmod.mixins.accessors.SplashesAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.resources.ResourceLocation;

public class MainMenuInjector {
    private static final int NO_PANORAMAS = 7;
    private static final ResourceLocation EDITION = new ResourceLocation(PortalMod.MODID, "textures/gui/title/edition.png");
    private static final ResourceLocation SPLASHES = new ResourceLocation(PortalMod.MODID, "texts/splashes.txt");
    private static ResourceLocation prevEdition;
    private static CubeMap prevCubeMap;
    private static ResourceLocation prevSplashes;
    public static boolean fading = true;
    public static boolean needsUpdate = true;
    
    public static TitleScreen getInjectedMenu(boolean custom, boolean fadeIn) {
        if(prevEdition == null)
            prevEdition = LogoRendererAccessor.pmGetEdition();
        if(prevCubeMap == null)
            prevCubeMap = MainMenuScreenAccessor.pmGetCubeMap();
        if(prevSplashes == null)
            prevSplashes = SplashesAccessor.pmGetLocation();

        CubeMap CUBEMAP = new CubeMap(new ResourceLocation(PortalMod.MODID,
                "textures/gui/title/background/panorama" + (long)(Math.random() * NO_PANORAMAS)));

        MainMenuScreenAccessor.pmSetCubeMap(custom ? CUBEMAP : prevCubeMap);

        if(!needsUpdate) return new TitleScreen(false);

        LogoRendererAccessor.pmSetEdition(custom ? EDITION : prevEdition);
        SplashesAccessor.pmSetLocation(custom ? SPLASHES : prevSplashes);

        try {
            Minecraft minecraft = Minecraft.getInstance();
            SplashManager splashes = new SplashManager(Minecraft.getInstance().getUser());

            List<String> splashList = ((SplashesAccessor)splashes).pmPrepare(minecraft.getResourceManager(), minecraft.getProfiler());
            ((SplashesAccessor)splashes).pmApply(splashList, minecraft.getResourceManager(), minecraft.getProfiler());
            ((MinecraftAccessor)Minecraft.getInstance()).pmSetSplashManager(splashes);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return new TitleScreen(false);
    }
}