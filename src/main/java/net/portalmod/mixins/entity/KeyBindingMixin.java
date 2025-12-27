package net.portalmod.mixins.entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

    // Prevent player movement while launched (#42)
    // I wish I could do this a better way, so that it worked with all entities and not just players, but of the dozens
    // of things I've tried, this is the only thing that has worked, and I've already sunk enough hours into this, so
    // I'm leaving it here.
    @Inject(
            method = "isDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onIsDown(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = Minecraft.getInstance().player;

        if (!(player instanceof IFaithPlateLaunchable)) return;
        if (!((IFaithPlateLaunchable) player).isLaunched()) return;

        KeyBinding self = (KeyBinding) (Object) this;

        boolean cancel = false;
        if (self.equals(Minecraft.getInstance().options.keyLeft))  cancel = true;
        if (self.equals(Minecraft.getInstance().options.keyRight)) cancel = true;
        if (self.equals(Minecraft.getInstance().options.keyUp))    cancel = true;
        if (self.equals(Minecraft.getInstance().options.keyDown))  cancel = true;

        if (cancel) cir.setReturnValue(false);
    }
}
