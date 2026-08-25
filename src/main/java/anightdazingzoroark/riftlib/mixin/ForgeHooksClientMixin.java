package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * mostly for armor rendering
 */
@Mixin(value = ForgeHooksClient.class, remap = false)
public abstract class ForgeHooksClientMixin {
    @Inject(method = "getArmorModel", at = @At("HEAD"), cancellable = true)
    private static void getArmorModel(EntityLivingBase wearer, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultArmor, CallbackInfoReturnable<ModelBiped> callback) {
        if (!(stack.getItem() instanceof ItemArmor armor)) return;

        GeoArmorRenderer<?> renderer = GeoArmorRenderer.getRenderer(armor);
        if (renderer == null) return;

        renderer.setCurrentItem(wearer, stack, slot);
        callback.setReturnValue(renderer.applyEntityStats(defaultArmor).applySlot(slot));
    }

    @Inject(method = "getArmorTexture", at = @At("HEAD"), cancellable = true)
    private static void getArmorTexture(Entity wearer, ItemStack stack, String defaultTexture, EntityEquipmentSlot slot, String type, CallbackInfoReturnable<String> callback) {
        if (!(stack.getItem() instanceof ItemArmor armor)) return;

        GeoArmorRenderer<?> renderer = GeoArmorRenderer.getRenderer(armor);
        if (renderer == null) return;

        if (wearer instanceof EntityLivingBase livingWearer) {
            callback.setReturnValue(renderer.getArmorTexture(livingWearer, stack, slot).toString());
        }
        else {
            callback.setReturnValue(renderer.getArmorTexture(stack).toString());
        }
    }
}
