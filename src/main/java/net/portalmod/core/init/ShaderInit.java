package net.portalmod.core.init;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import net.portalmod.client.render.Shader;
import net.portalmod.core.util.Registry;

public class ShaderInit {
    private ShaderInit() {}
    
    public static final Registry<Shader> REGISTRY = new Registry<>();
    
//    public static final Registry.Entry<Shader> PORTAL_DEFAULT = REGISTRY.register("portal_default",
//            () -> new Shader.Builder()
//            .add(GL_VERTEX_SHADER, "portal/vertex.vsh")
//            .add(GL_FRAGMENT_SHADER, "portal/fragment.fsh")
//            .build());
    
    public static final Registry.Entry<Shader> PORTAL_FRAME = REGISTRY.register("portal_frame",
            () -> new Shader.Builder()
            .add(GL_VERTEX_SHADER, "portal/vertex.vsh")
            .add(GL_FRAGMENT_SHADER, "portal/frame.fsh")
            .build());
    
    public static final Registry.Entry<Shader> PORTAL_HIGHLIGHT = REGISTRY.register("portal_highlight",
            () -> new Shader.Builder()
            .add(GL_VERTEX_SHADER, "portal/vertex.vsh")
            .add(GL_FRAGMENT_SHADER, "portal/highlight.fsh")
            .build());

    public static final Registry.Entry<Shader> FAITHPLATE_GUI = REGISTRY.register("faithplate_gui",
            () -> new Shader.Builder()
            .add(GL_VERTEX_SHADER, "gui/vertex.vsh")
            .add(GL_FRAGMENT_SHADER, "gui/fragment.fsh")
            .build());
    
    public static final Registry.Entry<Shader> FAITHPLATE_GRID = REGISTRY.register("faithplate_grid",
            () -> new Shader.Builder()
            .add(GL_VERTEX_SHADER, "gui/vertex.vsh")
            .add(GL_FRAGMENT_SHADER, "gui/grid.fsh")
            .build());

    public static final Registry.Entry<Shader> BLIT = REGISTRY.register("gui_blit",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "gui/blit.vsh")
                    .add(GL_FRAGMENT_SHADER, "gui/blit.fsh")
                    .build());

    public static final Registry.Entry<Shader> LOADER = REGISTRY.register("loader",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "gui/loader/loader.vsh")
                    .add(GL_FRAGMENT_SHADER, "gui/loader/loader.fsh")
                    .build());

    public static final Registry.Entry<Shader> COLOR = REGISTRY.register("color",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "color/color.vsh")
                    .add(GL_FRAGMENT_SHADER, "color/color.fsh")
                    .build());

    public static final Registry.Entry<Shader> ACTUAL_BLIT = REGISTRY.register("actual_blit",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "blit/blit.vsh")
                    .add(GL_FRAGMENT_SHADER, "blit/blit.fsh")
                    .build());

    public static final Registry.Entry<Shader> COLOR_PICKER_SV = REGISTRY.register("color_picker_sv",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "gui/color_picker/vertex.vsh")
                    .add(GL_FRAGMENT_SHADER, "gui/color_picker/sv.fsh")
                    .build());

    public static final Registry.Entry<Shader> COLOR_PICKER_HUE = REGISTRY.register("color_picker_hue",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "gui/color_picker/vertex.vsh")
                    .add(GL_FRAGMENT_SHADER, "gui/color_picker/hue.fsh")
                    .build());

    public static final Registry.Entry<Shader> COLOR_PICKER_SOLID = REGISTRY.register("color_picker_hue",
            () -> new Shader.Builder()
                    .add(GL_VERTEX_SHADER, "gui/color_picker/vertex.vsh")
                    .add(GL_FRAGMENT_SHADER, "gui/color_picker/solid.fsh")
                    .build());
}