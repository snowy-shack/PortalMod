package net.portalmod.common.sorted.portalgun;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.autoportal.AutoPortalBlock;
import net.portalmod.common.sorted.panel.PortalHelper;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.VolatilePortalHelper;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.math.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class PortalGunClient {
    private static PortalGunClient instance;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;
    private PressState lastUnresolvedPress = PressState.NONE;

    private static final int SHOOT_DELAY = 5;
    private long nextShot = 0;

    private static final int HELPER_DELAY = 15;
    private long nextHelp = 0;
    private UUID lastHelpedGun;
    private PortalEnd lastHelpedEnd;
    private BlockPos lastHelpedPos;
    private Direction lastHelpedFace;
    private VolatilePortalHelper lastHelpedHelper;

    private PortalGunClient() {}

    public static PortalGunClient getInstance() {
        if(instance == null)
            instance = new PortalGunClient();
        return instance;
    }

    private void handleLeftClick() {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if (player.hasPassenger(TestElementEntity.class)) {
            TestElementEntity.dropHeldEntities(player, true, false, player.getMainHandItem());
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.THROW_ENTITY).build());
        } else {
            this.shootPortal(PortalEnd.PRIMARY);
        }
    }

    private void handleRightClick() {
        if (!Minecraft.getInstance().player.hasPassenger(TestElementEntity.class)) {
            this.shootPortal(PortalEnd.SECONDARY);
        }
    }

    private void shootPortal(PortalEnd end) {
        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null)
            return;

        float partialTicks = Minecraft.getInstance().getFrameTime();
        Vector3d rayPath = player.getViewVector(partialTicks).scale(200);
        Vector3d from = player.getEyePosition(partialTicks);
        Vector3d to = from.add(rayPath);
        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);
        BlockRayTraceResult ray = PortalGun.customClip(player.level, rayCtx);

        PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL)
                .end(end).blockHit(ray).build());
    }

    public void tick() {
        if(this.nextShot > 0)
            this.nextShot--;

        if(this.nextHelp > 0)
            this.nextHelp--;

        if(this.canShoot()) {
            if(this.leftButtonPressed) {
                this.handleLeftClick();
                this.nextShot = SHOOT_DELAY;
            }

            if(rightButtonPressed) {
                this.handleRightClick();
                this.nextShot = SHOOT_DELAY;
            }
        }
    }

    private boolean canShoot() {
        return this.nextShot <= 0;
    }

    public boolean willBeHelped(UUID gun, PortalEnd end, BlockPos pos, Direction face, Direction horizontalDirection, World level) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Block frontBlock = level.getBlockState(pos.relative(face)).getBlock();

        if(this.nextHelp > 0 && !(frontBlock instanceof AutoPortalBlock)) {
            if(this.lastHelpedGun != null && this.lastHelpedEnd != null && this.lastHelpedPos != null && this.lastHelpedFace != null) {
                BlockState panelState = level.getBlockState(this.lastHelpedPos);
                Block panelBlock = panelState.getBlock();

                if(block instanceof PortalHelper && panelBlock instanceof PortalHelper) {
                    PortalHelper newHelper = (PortalHelper)block;
                    PortalHelper oldHelper = (PortalHelper)panelBlock;

                    if(newHelper.willHelpPortal(face, horizontalDirection, state, level)) {
                        boolean sameGun = gun.equals(this.lastHelpedGun);
                        boolean sameEnd = end.equals(this.lastHelpedEnd);
                        boolean samePanel = oldHelper.containsBlock(panelState, this.lastHelpedPos, pos, level);
                        boolean sameFace = face.equals(this.lastHelpedFace);

                        if(sameGun && sameEnd && samePanel && sameFace) {
                            return false;
                        }
                    }
                }
            }
        }

        return block instanceof PortalHelper && ((PortalHelper)block).willHelpPortal(face, horizontalDirection, state, level);
    }

    public boolean willBeHelped(UUID gun, PortalEnd end, Vec3 position, Direction face, VolatilePortalHelper helper) {
        if(this.nextHelp > 0) {
            if(this.lastHelpedGun != null && this.lastHelpedEnd != null && this.lastHelpedHelper != null) {
                boolean sameGun = gun.equals(this.lastHelpedGun);
                boolean sameEnd = end.equals(this.lastHelpedEnd);
                boolean sameHelper = this.lastHelpedHelper.equals(helper);

                if(sameGun && sameEnd && sameHelper) {
                    return false;
                }
            }
        }

        return helper.willHelpPortal(position, face);
    }

    public void setHelped(UUID gun, PortalEnd end, BlockPos pos, Direction face) {
        this.lastHelpedGun = gun;
        this.lastHelpedEnd = end;
        this.lastHelpedPos = pos;
        this.lastHelpedFace = face;
        this.nextHelp = HELPER_DELAY;
    }

    public void setHelped(UUID gun, PortalEnd end, VolatilePortalHelper helper) {
        this.lastHelpedGun = gun;
        this.lastHelpedEnd = end;
        this.lastHelpedHelper = helper;
        this.nextHelp = HELPER_DELAY;
    }

    public boolean handleMouseButtons(int button, int action) {
        boolean isItemFrame = Minecraft.getInstance().getEntityRenderDispatcher().crosshairPickEntity instanceof ItemFrameEntity;

        if(action == GLFW.GLFW_PRESS) {
            if(!Minecraft.getInstance().mouseHandler.isMouseGrabbed()) {
                Minecraft.getInstance().mouseHandler.grabMouse();
                return true;
            }
        }

        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.leftButtonPressed = action == GLFW.GLFW_PRESS;

            if(this.leftButtonPressed) {
                this.rightButtonPressed = false;

                if(canShoot()) {
                    this.handleLeftClick();
                    this.nextShot = SHOOT_DELAY;
                }
            }

            return true;
        }

        if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if(isItemFrame) {
                if(this.lastUnresolvedPress == PressState.PORTALGUN && action == GLFW.GLFW_RELEASE) {
                    this.lastUnresolvedPress = PressState.NONE;
                } else {
                    this.lastUnresolvedPress = PressState.ITEM_FRAME;
                    return false;
                }
            } else {
                if(this.lastUnresolvedPress == PressState.ITEM_FRAME && action == GLFW.GLFW_RELEASE) {
                    this.lastUnresolvedPress = PressState.NONE;
                    return false;
                } else {
                    this.lastUnresolvedPress = PressState.PORTALGUN;
                }
            }

            this.rightButtonPressed = action == GLFW.GLFW_PRESS;

            if(this.rightButtonPressed) {
                this.leftButtonPressed = false;

                if(canShoot()) {
                    this.handleRightClick();
                    this.nextShot = SHOOT_DELAY;
                }
            }

            return true;
        }

        return false;
    }

    private enum PressState {
        NONE,
        PORTALGUN,
        ITEM_FRAME
    }
}