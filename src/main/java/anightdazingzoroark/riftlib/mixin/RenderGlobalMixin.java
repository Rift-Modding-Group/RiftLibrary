package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRendererTicker;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * renders animated blocks with entities before translucent terrain
 * */
@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;preDrawBatch()V", remap = false))
    private void riftlib$renderAnimatedBlocks(Entity cameraEntity, ICamera camera, float partialTicks, CallbackInfo callback) {
        GeoBlockRendererTicker.INSTANCE.render(cameraEntity, camera, partialTicks);
    }
}
