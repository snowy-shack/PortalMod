package io.github.serialsniper.portalmod.client.render;

import io.github.serialsniper.portalmod.PortalMod;
import net.minecraft.client.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class PortalShaders {
    // todo add to reload listener
    private static final ResourceLocation VERTEX = new ResourceLocation(PortalMod.MODID, "shaders/vertex.vsh");
    private static final ResourceLocation FRAGMENT = new ResourceLocation(PortalMod.MODID, "shaders/fragment.fsh");
    private static final List<PortalShaders> REGISTRY = new ArrayList<>();
    private final ResourceLocation vertexLocation, fragmentLocation;
    private int id = -1;
    public PortalShaders() throws IOException {
        this(VERTEX, FRAGMENT);
    }
    public PortalShaders(ResourceLocation vertexLocation, ResourceLocation fragmentLocation) throws IOException {
        this.vertexLocation = vertexLocation;
        this.fragmentLocation = fragmentLocation;

        createProgram(vertexLocation, fragmentLocation);
        REGISTRY.add(this);
    }

    public PortalShaders(String vertexLocation, String fragmentLocation) throws IOException {
        this(new ResourceLocation(PortalMod.MODID, "shaders/" + vertexLocation + ".vsh"),
                new ResourceLocation(PortalMod.MODID, "shaders/" + fragmentLocation + ".fsh"));
    }

    public PortalShaders(String sameNameLocation) throws IOException {
        this(sameNameLocation, sameNameLocation);
    }
    public static void reloadAll() throws IOException {
        for(PortalShaders shader : REGISTRY)
            shader.reload();
    }
    public void reload() throws IOException {
        if(id != -1)
            glDeleteProgram(id);
        createProgram(vertexLocation, fragmentLocation);
    }

    // todo create location cache
    private static int getLocation(String name) {
        return glGetUniformLocation(currentShader, name);
    }
    public static void uniform1i(String name, int x) {
        glUniform1i(getLocation(name), x);
    }
    public static void uniform2i(String name, int x, int y) {
        glUniform2i(getLocation(name), x, y);
    }
    public static void uniform3i(String name, int x, int y, int z) {
        glUniform3i(getLocation(name), x, y, z);
    }
    public static void uniform4i(String name, int x, int y, int z, int w) {
        glUniform4i(getLocation(name), x, y, z, w);
    }
    public static void uniform1f(String name, float x) {
        glUniform1f(getLocation(name), x);
    }
    public static void uniform2f(String name, float x, float y) {
        glUniform2f(getLocation(name), x, y);
    }
    public static void uniform3f(String name, float x, float y, float z) {
        glUniform3f(getLocation(name), x, y, z);
    }
    public static void uniform4f(String name, float x, float y, float z, float w) {
        glUniform4f(getLocation(name), x, y, z, w);
    }
    public static void uniformMatrix(String name, float[] matrix) {
        glUniformMatrix4fv(getLocation(name), false, matrix);
    }
    public static void uniformMatrix(String name, FloatBuffer matrix) {
        glUniformMatrix4fv(getLocation(name), false, matrix);
    }
    private void createProgram(ResourceLocation vertexLocation, ResourceLocation fragmentLocation) throws IOException {
        this.id = glCreateProgram();

        int vertex = compileShader(GL_VERTEX_SHADER, vertexLocation);
        int fragment = compileShader(GL_FRAGMENT_SHADER, fragmentLocation);

        glAttachShader(id, vertex);
        glAttachShader(id, fragment);

        glLinkProgram(id);
        glValidateProgram(id);

        glDeleteShader(vertex);
        glDeleteShader(fragment);

        PortalMod.LOGGER.info("Registered PortalMod shader with id: " + id);
    }
    private int compileShader(int type, ResourceLocation path) throws IOException {
        String shader = readSource(path);

        int id = glCreateShader(type);

        glShaderSource(id, shader);
        glCompileShader(id);

        int[] result = new int[1];
        glGetShaderiv(id, GL_COMPILE_STATUS, result);

        if(result[0] == GL_FALSE) {
            int[] length = new int[1];
            glGetShaderiv(id, GL_INFO_LOG_LENGTH, length);
            String message = glGetShaderInfoLog(id, length[0]);

            glDeleteShader(id);

            PortalMod.LOGGER.error(message);
            throw new IllegalStateException("Failed to compile " + (type == GL_VERTEX_SHADER ? "vertex" : "fragment") + " shader");
        }

        return id;
    }
    private String readSource(ResourceLocation path) throws IOException {
        BufferedReader reader;
        StringBuilder sourceBuilder = new StringBuilder();

        reader = new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(path).getInputStream()));

        String line;
        while((line = reader.readLine()) != null)
            sourceBuilder.append(line + "\n");

        reader.close();

        return sourceBuilder.toString();
    }
    private static int currentShader, oldShader;
    public void bind() {
        currentShader = id;
        oldShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        glUseProgram(id);
    }
    public void unbind() {
        glUseProgram(oldShader);
    }
    public int getId() {
        return id;
    }
}