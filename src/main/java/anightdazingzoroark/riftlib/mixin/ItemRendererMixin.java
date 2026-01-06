package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private ItemStack lastItemStackMainHand = ItemStack.EMPTY;
    private ItemStack lastItemStackOffHand = ItemStack.EMPTY;

    @Inject(method = "updateEquippedItem", at = @At(value = "TAIL"))
    public void updateEquippedItem(CallbackInfo ci) {
        ItemRenderer thisItemRenderer = (ItemRenderer) ((Object) this);

        if (!thisItemRenderer.itemStackMainHand.equals(this.lastItemStackMainHand)
                && this.lastItemStackMainHand.getItem() instanceof IAnimatable) {
            System.out.println("start locator removal");
            this.clearItemAnimationData(this.lastItemStackMainHand);
        }

        this.lastItemStackMainHand = thisItemRenderer.itemStackMainHand;
    }

    private void clearItemAnimationData(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof IAnimatable)) return;
        IAnimatable itemAnimatable = (IAnimatable) itemStack.getItem();

        //get geoanimatedlocator
        TileEntityItemStackRenderer itemStackRenderer = itemStack.getItem().getTileEntityItemStackRenderer();
        if (!(itemStackRenderer instanceof GeoItemRenderer)) return;
        GeoItemRenderer geoItemRenderer = (GeoItemRenderer) itemStackRenderer;

        if (geoItemRenderer.getCurrentItemStack() == null
                || !geoItemRenderer.getCurrentItemStack().equals(itemStack)) return;

        Integer uniqueID = geoItemRenderer.getUniqueID(itemStack.getItem());
        itemAnimatable.getFactory().removeAnimationData(uniqueID);
    }
}
