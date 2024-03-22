package net.portalmod.client.animation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.portalmod.core.util.Colour;

public class AnimatedModel extends Model {
    private final Map<String, AnimationSet> animations = new HashMap<>();
    private final Map<ModelRenderer, InitialValuesHolder> initialValues = new HashMap<>();
    private final Map<Object, AnimationState> animationStates = new HashMap<>();

    public static final Target X = (model, x) -> model.x = x;
    public static final Target Y = (model, y) -> model.y = y;
    public static final Target Z = (model, z) -> model.z = z;
    public static final Target XROT = (model, xRot) -> model.xRot = xRot;
    public static final Target YROT = (model, yRot) -> model.yRot = yRot;
    public static final Target ZROT = (model, zRot) -> model.zRot = zRot;

    public AnimatedModel(int texWidth, int texHeight) {
        super(RenderType::entityCutoutNoCull);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    protected void attachAnimation(String name, ModelRenderer model, Target target, Animation animation) {
        initialValues.put(model, new InitialValuesHolder(model));
        animations.putIfAbsent(name, new AnimationSet());
        animations.get(name).putIfAbsent(model, new HashMap<>());
        animations.get(name).get(model).put(target, animation);
    }

    private long getMillis() {
        return System.currentTimeMillis();
    }

    public void startAnimation(Object entity, String name) {
        animationStates.put(entity, new AnimationState(animations.get(name), getMillis()));
    }

    public void startAnimation(String name) {
        startAnimation(null, name);
    }

    private void computeAnimation(Object entity) {
        AnimationState state = animationStates.get(entity);

        if(state == null) {
            skipAnimation();
            return;
        }

        long delta = getMillis() - state.time;
        state.animationSet.forEach((model, map) ->
            map.forEach((target, animation) ->
                target.accept(model, (float)animation.computeAbs(delta))));
    }

    private void computeAnimation() {
        computeAnimation(null);
    }

    private void skipAnimation() {
        initialValues.forEach((model, currentInitialValues) -> {
            model.x = currentInitialValues.x;
            model.y = currentInitialValues.y;
            model.z = currentInitialValues.z;
            model.xRot = currentInitialValues.xRot;
            model.yRot = currentInitialValues.yRot;
            model.zRot = currentInitialValues.zRot;
        });
    }

    public void render(Object entity, ModelRenderer model, MatrixStack matrixStack, IVertexBuilder vertexBuilder, int light, int overlay, Colour colour, boolean skipAnimation) {
        if(skipAnimation)
            skipAnimation();
        else
            computeAnimation(entity);

        model.render(matrixStack, vertexBuilder, light, overlay,
                colour.getFloatR(), colour.getFloatG(), colour.getFloatB(), colour.getFloatA());
    }

    private static class InitialValuesHolder {
        float x, y, z;
        float xRot, yRot, zRot;

        private InitialValuesHolder(ModelRenderer model) {
            this.x = model.x;
            this.y = model.y;
            this.z = model.z;
            this.xRot = model.xRot;
            this.yRot = model.yRot;
            this.zRot = model.zRot;
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int light, int overlay, float r, float g, float b, float a) {}

    private interface Target extends BiConsumer<ModelRenderer, Float> {}

    private static final class AnimationSet extends HashMap<ModelRenderer, HashMap<Target, Animation>> {}

    private static final class AnimationState {
        public AnimationSet animationSet;
        public long time;

        public AnimationState(AnimationSet animationSet, long time) {
            this.animationSet = animationSet;
            this.time = time;
        }
    }
}