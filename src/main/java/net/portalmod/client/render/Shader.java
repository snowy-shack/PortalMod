package net.portalmod.client.render;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform2i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform3i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniform4i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.portalmod.PortalMod;

public class Shader {
    private static final String BASE_PATH = "shaders/";
    private static final List<Shader> REGISTRY = new ArrayList<>();
    
    private final Map<Integer, String> sources;
    private int id;
    
    private Shader(int id, Map<Integer, String> sources) {
        this.id = id;
        this.sources = sources;
        REGISTRY.add(this);
    }
    
    public Shader bind() {
        glUseProgram(id);
        return this;
    }
    
    public void unbind() {
        glUseProgram(0);
    }
    
    public int getId() {
        return id;
    }
    
    public static void reloadAll() throws IOException {
        for(Shader shader : REGISTRY)
            shader.reload();
    }
    
    public void reload() throws IOException {
        Builder builder = new Builder();
        sources.forEach((type, path) -> builder.add(type, path));
        int newId = builder.makeShader(true);

        if(newId < 0) {
            PortalMod.LOGGER.error("Failed to reload shader [ID: " + id + "], skipping...");
            return;
        }

        glDeleteProgram(id);
        id = newId;
    }
    
    public static class Builder {
        private final Map<Integer, String> sources;
        private final List<Integer> ids;
        
        public Builder() {
            this.sources = new HashMap<>();
            this.ids = new ArrayList<>();
        }
        
        public Builder add(int type, String path) {
            if(sources.containsKey(type)) {
                PortalMod.LOGGER.error("Failed to attach shader of type " + type + " as it already exists in this program");
                return this;
            }

            int id = glCreateShader(type);
            glShaderSource(id, load(path));
            glCompileShader(id);

            if(glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
                PortalMod.LOGGER.error("Failed to compile shader [ID: " + id + "]");
                PortalMod.LOGGER.error(glGetShaderInfoLog(id, glGetShaderi(id, GL_INFO_LOG_LENGTH)));
                System.exit(1);
            }

            sources.put(type, path);
            ids.add(id);
            return this;
        }
        
        private String load(String path) {
            try {
                StringBuilder sourceBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Minecraft.getInstance().getResourceManager().getResource(
                                new ResourceLocation(PortalMod.MODID, BASE_PATH + path)).getInputStream()));

                String line;
                while((line = reader.readLine()) != null)
                    sourceBuilder.append(line + "\n");

                reader.close();
                return sourceBuilder.toString();
            } catch(Exception e) {
                PortalMod.LOGGER.error(e);
                System.exit(1);
                return "";
            }
        }
        
        private int makeShader(boolean skipIfFailed) {
            int programId = glCreateProgram();

            for(int id : ids)
                glAttachShader(programId, id);

            glLinkProgram(programId);
            glValidateProgram(programId);

            if(glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
                PortalMod.LOGGER.error("Failed to compile shader program [ID: " + programId + "]");
                PortalMod.LOGGER.error(glGetProgramInfoLog(programId, glGetProgrami(programId, GL_INFO_LOG_LENGTH)));

                if(skipIfFailed)
                    return -1;
                else
                    System.exit(1);
            }

            for(int id : ids)
                glDeleteShader(id);
            
            PortalMod.LOGGER.info("Loaded PortalMod shader [ID: " + programId + "]");
            return programId;
        }
        
        public Shader build() {
            return new Shader(makeShader(false), sources);
        }
    }
    
    private final Map<String, Integer> UNIFORM_CACHE = new HashMap<>();
    
    private int getUniformLocation(String name) {
        if(!UNIFORM_CACHE.containsKey(name))
            UNIFORM_CACHE.put(name, glGetUniformLocation(id, name));
        return UNIFORM_CACHE.get(name);
    }

    public Shader setInt(String name, int x) {
        glUniform1i(getUniformLocation(name), x);
        return this;
    }

    public Shader setInt(String name, int x, int y) {
        glUniform2i(getUniformLocation(name), x, y);
        return this;
    }

    public Shader setInt(String name, int x, int y, int z) {
        glUniform3i(getUniformLocation(name), x, y, z);
        return this;
    }

    public Shader setInt(String name, int x, int y, int z, int w) {
        glUniform4i(getUniformLocation(name), x, y, z, w);
        return this;
    }

    public Shader setFloat(String name, float x) {
        glUniform1f(getUniformLocation(name), x);
        return this;
    }

    public Shader setFloat(String name, float x, float y) {
        glUniform2f(getUniformLocation(name), x, y);
        return this;
    }

    public Shader setFloat(String name, float x, float y, float z) {
        glUniform3f(getUniformLocation(name), x, y, z);
        return this;
    }

    public Shader setFloat(String name, float x, float y, float z, float w) {
        glUniform4f(getUniformLocation(name), x, y, z, w);
        return this;
    }
    
    private final FloatBuffer MATRIX_BUFFER = MemoryUtil.memAllocFloat(16);
    
    public Shader setMatrix(String name, FloatBuffer matrix) {
        glUniformMatrix4fv(getUniformLocation(name), false, matrix);
        return this;
    }
    
    public Shader setMatrix(String name, Matrix4f matrix) {
        matrix.store(MATRIX_BUFFER);
        setMatrix(name, MATRIX_BUFFER);
        return this;
    }
}