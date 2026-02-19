package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.core.init.PacketInit;
import org.lwjgl.glfw.GLFW;

public class PortalGunClient {
    private static boolean leftButtonPressed = false;
    private static boolean rightButtonPressed = false;
    private static long nextShot = 0;
    private static final int SHOOT_DELAY = 5;
    private static PressState lastUnresolvedPress = PressState.NONE;

    protected static void handleLeftClick() {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if (player.hasPassenger(TestElementEntity.class)) {
            TestElementEntity.dropHeldEntities(player, true, false, player.getMainHandItem());

            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.THROW_ENTITY).build());
        } else PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.PRIMARY).build());
    }

    protected static void handleRightClick() {
        if (!Minecraft.getInstance().player.hasPassenger(TestElementEntity.class))
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.SHOOT_PORTAL).end(PortalEnd.SECONDARY).build());
    }

    public static void tick() {
        if(nextShot > 0)
            nextShot--;

        if(canShoot()) {
            if(leftButtonPressed) {
                PortalGun.handleLeftClick();
                nextShot = SHOOT_DELAY;
            }

            if(rightButtonPressed) {
                PortalGun.handleRightClick();
                nextShot = SHOOT_DELAY;
            }
        }
    }

    private static boolean canShoot() {
        return nextShot <= 0;
    }

    public static boolean handleMouseButtons(int button, int action) {
        boolean isItemFrame = Minecraft.getInstance().getEntityRenderDispatcher().crosshairPickEntity instanceof ItemFrameEntity;

        if(action == GLFW.GLFW_PRESS) {
            Minecraft.getInstance().mouseHandler.grabMouse();
        }

        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftButtonPressed = action == GLFW.GLFW_PRESS;

            if(leftButtonPressed) {
                rightButtonPressed = false;

                if(canShoot()) {
                    PortalGun.handleLeftClick();
                    nextShot = SHOOT_DELAY;
                }
            }

            return true;
        }

        if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if(isItemFrame) {
                if(lastUnresolvedPress == PressState.PORTALGUN && action == GLFW.GLFW_RELEASE) {
                    lastUnresolvedPress = PressState.NONE;
                } else {
                    lastUnresolvedPress = PressState.ITEM_FRAME;
                    return false;
                }
            } else {
                if(lastUnresolvedPress == PressState.ITEM_FRAME && action == GLFW.GLFW_RELEASE) {
                    lastUnresolvedPress = PressState.NONE;
                    return false;
                } else {
                    lastUnresolvedPress = PressState.PORTALGUN;
                }
            }

            rightButtonPressed = action == GLFW.GLFW_PRESS;

            if(rightButtonPressed) {
                leftButtonPressed = false;

                if(canShoot()) {
                    PortalGun.handleRightClick();
                    nextShot = SHOOT_DELAY;
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