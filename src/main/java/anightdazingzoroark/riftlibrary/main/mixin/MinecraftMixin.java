package anightdazingzoroark.riftlibrary.main.mixin;

import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla mixin example
 * Refmap will be handled by Unimined automatically
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "createDisplay", at = @At("HEAD"))
    public void inject(CallbackInfo ci){
        RiftLibraryMod.LOGGER.info("Mixin succeed!");
    }
}
