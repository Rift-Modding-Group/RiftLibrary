package anightdazingzoroark.riftlib.renderers.geo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.apache.commons.lang3.tuple.MutablePair;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class GeoItemRenderer<T extends AnimatedItemStackHolder> extends TileEntityItemStackRenderer implements IGeoRenderer<T> {
	/**
	 * these constants are for cleaning up cache related to rendering of itemstacks
	 */
	private static final long HOLDER_CACHE_TTL_MS = 10000L;
	private static final long HOLDER_CACHE_CLEANUP_INTERVAL_MS = 1000L;
	private static final int HOLDER_CACHE_MAX_SIZE = 256;

	// Register a model fetcher for this renderer
	static {
		AnimationController.addModelFetcher((IAnimatable<?> object) -> {
			if (object instanceof AnimatedItemStackHolder holder) {
				Item item = holder.getStack().getItem();
				TileEntityItemStackRenderer renderer = item.getTileEntityItemStackRenderer();
				if (renderer instanceof GeoItemRenderer) {
					return (IAnimatableModel<Object>) ((GeoItemRenderer<?>) renderer).getGeoModelProvider();
				}
			}
			return null;
		});
	}

	private final AnimatedGeoModel<T> modelProvider;
	private final Function<ItemStack, T> holderCreator;
	//this map holds data for individual itemstacks and removes unrendered items every now and then
	//key is the itemstack render identity, value is the animated itemstack holder and last render time
	private final Map<Integer, MutablePair<T, Long>> holderCache = new HashMap<>();
	private long lastHolderCacheCleanup;

	public GeoItemRenderer(AnimatedGeoModel<T> modelProvider, Function<ItemStack, T> holderCreator) {
		this.modelProvider = modelProvider;
		this.holderCreator = holderCreator;
		GeoItemRendererTicker.ITEM_RENDERERS.add(this);
	}

	@Override
	public AnimatedGeoModel<T> getGeoModelProvider() {
		return this.modelProvider;
	}

	protected T getOrCreateHolder(ItemStack itemStack, ItemCameraTransforms.TransformType transformType) {
		long now = Minecraft.getSystemTime();
		this.cleanupHolderCache(now);

		int key = this.getHolderCacheKey(itemStack, transformType);
		MutablePair<T, Long> entry = this.holderCache.get(key);
		T holder;
		if (entry == null) {
			holder = this.holderCreator.apply(itemStack.copy());
			entry = new MutablePair<>(holder, now);
			this.holderCache.put(key, entry);
		}
		else {
			holder = entry.getLeft();
			holder.setStack(itemStack.copy());
			entry.setRight(now);
		}

		return holder;
	}

	//cache for instances of an item. totally different from uniqueID from before
	protected int getHolderCacheKey(ItemStack itemStack, ItemCameraTransforms.TransformType transformType) {
		return Objects.hash(
				itemStack.getItem(),
				itemStack.getMetadata(),
				itemStack.hasTagCompound() ? itemStack.getTagCompound().toString() : null,
				transformType
		);
	}

	public void cleanupHolderCache(long now) {
		if (now - this.lastHolderCacheCleanup < HOLDER_CACHE_CLEANUP_INTERVAL_MS) return;
		this.lastHolderCacheCleanup = now;

        this.holderCache.entrySet().removeIf(entry -> now - entry.getValue().getRight() > HOLDER_CACHE_TTL_MS);

		while (this.holderCache.size() > HOLDER_CACHE_MAX_SIZE) {
			Integer oldestKey = null;
			long oldestSeen = Long.MAX_VALUE;
			for (Map.Entry<Integer, MutablePair<T, Long>> entry : this.holderCache.entrySet()) {
				if (entry.getValue().getRight() < oldestSeen) {
					oldestSeen = entry.getValue().getRight();
					oldestKey = entry.getKey();
				}
			}
			if (oldestKey == null) return;
			this.holderCache.remove(oldestKey);
		}
	}

	public void render(ItemStack itemStack, ItemCameraTransforms.TransformType transformType) {
		T animatable = this.getOrCreateHolder(itemStack, transformType);

		GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(animatable));
		AnimationEvent itemEvent = new AnimationEvent(
				Minecraft.getMinecraft().getRenderPartialTicks(),
				Collections.singletonList(itemStack)
		);
		this.modelProvider.setLivingAnimations(animatable, itemEvent);
		if (transformType != ItemCameraTransforms.TransformType.GUI) this.modelProvider.createAndUpdateAnimatedLocators(animatable);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.01f, 0);
		GlStateManager.translate(0.5, 0.5, 0.5);

		Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(animatable));
		Color renderColor = this.getRenderColor(animatable, 0f);
        this.render(model, animatable, 0f,
                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
				(float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255
		);
		GlStateManager.popMatrix();
	}

	@Override
	public ResourceLocation getTextureLocation(T instance) {
		return this.modelProvider.getTextureLocation(instance);
	}
}
