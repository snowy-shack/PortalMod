package net.portalmod.core.injectors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.util.Splashes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.portalmod.PortalMod;
import net.portalmod.mixins.accessors.MainMenuScreenAccessor;
import net.portalmod.mixins.accessors.MinecraftAccessor;
import net.portalmod.mixins.accessors.SplashesAccessor;

public class MainMenuInjector {
    private static final int NO_PANORAMAS = 1;
    private static final ResourceLocation EDITION = new ResourceLocation(PortalMod.MODID, "textures/gui/title/edition.png");
    private static final ResourceLocation SPLASHES = new ResourceLocation(PortalMod.MODID, "texts/splashes.txt");
    private static ResourceLocation prevEdition;
    private static RenderSkyboxCube prevCubeMap;
    private static ResourceLocation prevSplashes;
    public static boolean fading = true;
    public static boolean needsUpdate = true;
    
    public static MainMenuScreen getInjectedMenu(boolean custom, boolean fadeIn) {
        if(prevEdition == null)
            prevEdition = MainMenuScreenAccessor.pmGetEdition();
        if(prevCubeMap == null)
            prevCubeMap = MainMenuScreenAccessor.pmGetCubeMap();
        if(prevSplashes == null)
            prevSplashes = SplashesAccessor.pmGetLocation();

        RenderSkyboxCube CUBEMAP = new RenderSkyboxCube(new ResourceLocation(PortalMod.MODID,
                "textures/gui/title/background/panorama" + (long)(Math.random() * NO_PANORAMAS)));

        MainMenuScreenAccessor.pmSetCubeMap(custom ? CUBEMAP : prevCubeMap);

        if(!needsUpdate) return new MainMenuScreen(false);

        MainMenuScreenAccessor.pmSetEdition(custom ? EDITION : prevEdition);
        SplashesAccessor.pmSetLocation(custom ? SPLASHES : prevSplashes);

        try {
            Minecraft minecraft = Minecraft.getInstance();
            Splashes splashes = new Splashes(Minecraft.getInstance().getUser());

            List<String> splashList = ((SplashesAccessor)splashes).pmPrepare(minecraft.getResourceManager(), minecraft.getProfiler());
            ((SplashesAccessor)splashes).pmApply(splashList, minecraft.getResourceManager(), minecraft.getProfiler());
            ((MinecraftAccessor)Minecraft.getInstance()).pmSetSplashManager(splashes);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return new MainMenuScreen(false);
    }
}