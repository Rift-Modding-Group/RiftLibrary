package anightdazingzoroark.riftlib.renderers.geo;

import java.util.Collections;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class GeoItemRenderer<T extends Item & IAnimatable> extends TileEntityItemStackRenderer
		implements IGeoRenderer<T> {
	// Register a model fetcher for this renderer
	static {
		AnimationController.addModelFetcher((IAnimatable object) -> {
			if (object instanceof Item) {
				Item item = (Item) object;
				TileEntityItemStackRenderer renderer = item.getTileEntityItemStackRenderer();
				if (renderer instanceof GeoItemRenderer) {
					return (IAnimatableModel<Object>) ((GeoItemRenderer<?>) renderer).getGeoModelProvider();
				}
			}
			return null;
		});
	}

	protected AnimatedGeoModel<T> modelProvider;
	protected ItemStack currentItemStack;
    private boolean canRenderParticles;

	public GeoItemRenderer(AnimatedGeoModel<T> modelProvider) {
		this.modelProvider = modelProvider;
	}

	public void setModel(AnimatedGeoModel<T> model) {
		this.modelProvider = model;
	}

	@Override
	public AnimatedGeoModel<T> getGeoModelProvider() {
		return this.modelProvider;
	}

    public void setCanRenderParticles(boolean value) {
        this.canRenderParticles = value;
    }

	@Override
	public void renderByItem(ItemStack itemStack, float partialTicks) {
		this.render((T) itemStack.getItem(), itemStack);
	}

	public void render(T animatable, ItemStack itemStack) {
		this.currentItemStack = itemStack;
		GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(animatable));
        Integer uniqueID = this.getUniqueID(animatable);
		AnimationEvent itemEvent = new AnimationEvent(animatable, 0, 0,
				Minecraft.getMinecraft().getRenderPartialTicks(), false, Collections.singletonList(itemStack));
		this.modelProvider.setLivingAnimations(animatable, uniqueID, itemEvent);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.01f, 0);
		GlStateManager.translate(0.5, 0.5, 0.5);

		Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(animatable));
		Color renderColor = getRenderColor(animatable, 0f);
        this.render(model, animatable, uniqueID, 0f, this.canRenderParticles,
                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
				(float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
		GlStateManager.popMatrix();
	}

	@Override
	public ResourceLocation getTextureLocation(T instance) {
		return this.modelProvider.getTextureLocation(instance);
	}

	@Override
	public Integer getUniqueID(T animatable) {
		return Objects.hash(
                this.currentItemStack.getItem(),
                this.currentItemStack.getCount(),
                this.currentItemStack.hasTagCompound() ? this.currentItemStack.getTagCompound().toString() : 1,
                this.currentItemStack.hashCode()
        );
	}
}
