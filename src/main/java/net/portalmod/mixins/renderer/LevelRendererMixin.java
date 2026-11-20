package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.config.PortalModConfigManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    public boolean fizzlerIsAirToo(BlockState instance, BlockGetter blockReader, BlockPos pos) {
        return instance.isAir(blockReader, pos) || instance.getBlock() instanceof FizzlerFieldBlock;
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
            )
    )
    private void pmClear(int buffers, boolean onOsx) {
        if(PortalRenderer.getInstance().recursion == 0) {
            GL11.glStencilMask(~0);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT, onOsx);
        }
    }

    @Redirect(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FogRenderer;setupColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)V"
            )
    )
    private void pmSetupColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darken) {
        if(PortalRenderer.getInstance().currentlyRenderingPortals) {
            PortalEntity currentPortal = PortalRenderer.getInstance().portalChain.peekLast();
            if(currentPortal != null && currentPortal.getOtherPortal().isPresent()) {
                camera = new PortalCamera(camera, partialTicks);
                camera.setPosition(currentPortal.getOtherPortal().get().position());
            }
        }

        FogRenderer.setupColor(camera, partialTicks, level, renderDistance, darken);
    }

    @Redirect(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FogRenderer;setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V"
            )
    )
    private void pmSetupFog(Camera camera, FogRenderer.FogType type, float renderDistance, boolean b, float partialTicks) {
        if(PortalRenderer.getInstance().currentlyRenderingPortals) {
            PortalEntity currentPortal = PortalRenderer.getInstance().portalChain.peekLast();
            if(currentPortal != null && currentPortal.getOtherPortal().isPresent()) {
                camera = new PortalCamera(camera, partialTicks);
                camera.setPosition(currentPortal.getOtherPortal().get().position());
            }
        }

        FogRenderer.setupFog(camera, type, renderDistance, b, partialTicks);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;constantAmbientLight()Z"
            )
    )
    private void pmRenderPortals(PoseStack matrixStack, float partialTicks, long l, boolean b, Camera camera, GameRenderer gr, LightTexture lt, Matrix4f matrix, CallbackInfo info) {
        ClientLevel level = Minecraft.getInstance().level;

        Vec3 vector3d = camera.getPosition();
        double x = vector3d.x();
        double y = vector3d.y();
        double z = vector3d.z();
        Matrix4f matrix4f = matrixStack.last().pose();
        Frustum clippinghelper = new Frustum(matrix4f, matrix);
        clippinghelper.prepare(x, y, z);

        PortalRenderer.getInstance().renderPortals(level, camera, clippinghelper, matrix, partialTicks);
    }

    // BEWARE: PORTAL RENDERING
    @ModifyArgs(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDD)V"
            )
    )
    private void pmSwitchToPortalTransparency(Args args) {
        RenderType renderType = args.get(0);
        if(renderType == RenderType.translucent() && PortalRenderer.getInstance().currentlyRenderingPortals)
            args.set(0, PortalTransparencyHandler.PORTAL_TRANSLUCENT);
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;translucent()Lnet/minecraft/client/renderer/RenderType;",
                    ordinal = 1
            )
    )
    private RenderType pmRenderPortalTransparencyBackwards() {
        return PortalRenderer.getInstance().currentlyRenderingPortals ? PortalTransparencyHandler.PORTAL_TRANSLUCENT : RenderType.translucent();
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;isEmpty(Lnet/minecraft/client/renderer/RenderType;)Z"
            )
    )
    private boolean pmIsPortalTransparencyEmpty(ChunkRenderDispatcher.CompiledChunk compiledChunk, RenderType renderType) {
        return compiledChunk.isEmpty(renderType == PortalTransparencyHandler.PORTAL_TRANSLUCENT ? RenderType.translucent() : renderType);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
                        method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V",
                    shift = At.Shift.AFTER
            )
    )
    private void pmResortPortalTransparency(RenderType renderType, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci) {
        // todo probably optimize a bit
        // todo do asynchronously
        if(renderType == PortalTransparencyHandler.PORTAL_TRANSLUCENT) {
            Minecraft.getInstance().getProfiler().push("pm_translucent_sort");
            PortalTransparencyHandler.resortTransparency(PortalRenderer.getInstance().currentCamera);
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "setupRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;setCamera(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void pmUseMainCameraForRebuilding(ChunkRenderDispatcher instance, Vec3 pos) {
        instance.setCamera(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
    }

    /**
     * Mask the {@code if (options.renderDistance != lastViewDistance) allChanged();} guard
     * at the top of {@code setupRender} while a nested portal render has intentionally
     * clamped {@code lastViewDistance}. ordinal = 0 targets exactly this GETFIELD: the
     * second access of {@code renderDistance} in the same method (which feeds
     * {@code Entity.setViewScale}) is left alone so entity view scale stays consistent
     * with the player's real render distance setting.
     */
    @Redirect(
            method = "setupRender",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Options;renderDistance:I",
                    opcode = org.objectweb.asm.Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int pmMaskRenderDistanceForAllChangedGuard(Options instance) {
        int override = PortalRenderer.getInstance().nestedBfsDistanceOverride;
        return override > 0 ? override : instance.renderDistance;
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean pmAvoidRenderingOutlineInFakeEntities(Minecraft instance, Entity entity) {
        if(PortalRenderer.getInstance().recursion > 0)
            return false;
        return instance.shouldEntityAppearGlowing(entity);
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;isDetached()Z"
            )
    )
    private boolean pmRenderSelf(Camera instance) {
        if(!PortalModConfigManager.RENDER_SELF.get())
            return instance.isDetached();
        return true;
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"
            )
    )
    private boolean pmShouldRenderSelf(EntityRenderDispatcher erm, Entity entity, Frustum clippingHelper, double camX, double camY, double camZ) {
        if(!PortalModConfigManager.RENDER_SELF.get() || entity != Minecraft.getInstance().cameraEntity)
            return erm.shouldRender(entity, clippingHelper, camX, camY, camZ);
        return DuplicateEntityRenderer.shouldRenderSelf(entity, clippingHelper);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
                        method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 10
            )
    )
    private void pmRenderDuplicateEntities(PoseStack matrixStack, float partialTicks, long l, boolean b, Camera camera, GameRenderer gr, LightTexture lt, Matrix4f projectionMatrix, CallbackInfo info) {
        Vec3 vector3d = camera.getPosition();
        double camX = vector3d.x();
        double camY = vector3d.y();
        double camZ = vector3d.z();

        Frustum clippinghelper = new Frustum(matrixStack.last().pose(), projectionMatrix);
        clippinghelper.prepare(camX, camY, camZ);

        DuplicateEntityRenderer.renderDuplicateEntities(clippinghelper, camX, camY, camZ, partialTicks, matrixStack, camera);
    }
}