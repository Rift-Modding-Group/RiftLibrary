package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin is mainly for blocking particle rendering when
 * the item is rendered in a UI. Soon however this may be
 * modified to permit rendering particles in certain UIs
 * **/
@Mixin(RenderItem.class)
public class RenderItemMixin {
    @Inject(method = "renderItemModel", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        RenderItem thisRenderItem = (RenderItem) ((Object) this);
        Item item = stack.getItem();
        if (!stack.isEmpty() && item instanceof IAnimatable) {
            thisRenderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            thisRenderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, transform, leftHanded);

            this.finalRenderItem(stack, bakedmodel, transform);
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            thisRenderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            thisRenderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

            ci.cancel();
        }
    }

    @Inject(method = "renderItemModelIntoGUI", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel, CallbackInfo ci) {
        RenderItem thisRenderItem = (RenderItem) ((Object) this);
        Item item = stack.getItem();
        if (item instanceof IAnimatable) {
            GlStateManager.pushMatrix();
            thisRenderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            thisRenderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            thisRenderItem.setupGuiTransform(x, y, bakedmodel.isGui3d());
            bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
            this.finalRenderItem(stack, bakedmodel, ItemCameraTransforms.TransformType.GUI);
            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();
            thisRenderItem.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            thisRenderItem.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

            ci.cancel();
        }
    }

    private void finalRenderItem(ItemStack stack, IBakedModel model, ItemCameraTransforms.TransformType transformType) {
        RenderItem thisRenderItem = (RenderItem) ((Object) this);
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            if (model.isBuiltInRenderer()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                GeoItemRenderer geoItemRenderer = (GeoItemRenderer) stack.getItem().getTileEntityItemStackRenderer();
                geoItemRenderer.render(stack.getItem(), stack, transformType);
            }
            else {
                thisRenderItem.renderModel(model, stack);
                if (stack.hasEffect()) thisRenderItem.renderEffect(model);
            }

            GlStateManager.popMatrix();
        }
    }
}
