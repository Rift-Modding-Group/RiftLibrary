package anightdazingzoroark.riftlib.renderers.geo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import anightdazingzoroark.riftlib.item.EntityItemRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked" })
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
	private final Map<UUID, EnumMap<EnumHand, Integer>> equippedItemIdentities = new HashMap<>();
	private final Map<UUID, EnumMap<EnumHand, Integer>> equipSessions = new HashMap<>();
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

		holder.setTransformType(transformType);
		Entity entity = this.getEntityHolder(itemStack, transformType);
		EnumHand hand = null;
		if (entity instanceof EntityLivingBase living) {
			if (living.getHeldItemMainhand() == itemStack) hand = EnumHand.MAIN_HAND;
			else if (living.getHeldItemOffhand() == itemStack) hand = EnumHand.OFF_HAND;
			else if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
					|| transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
				EnumHandSide side = transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
						? EnumHandSide.RIGHT : EnumHandSide.LEFT;
				hand = living.getPrimaryHand() == side ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			}
		}
		holder.getAnimationData().setRenderContext(entity, hand);
		return holder;
	}

	/**
	 * Resolves by render context or stack identity so identical items held by different entities stay independent.
	 * */
	@Nullable
	protected Entity getEntityHolder(@NotNull ItemStack stack, @NotNull ItemCameraTransforms.TransformType transformType) {
		if (transformType == ItemCameraTransforms.TransformType.GUI) return null;
		EntityItemRenderContext context = EntityItemRenderContext.getCurrent();
		if (context != null) return context.getEntity();
		Minecraft minecraft = Minecraft.getMinecraft();
		if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
				|| transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) return minecraft.player;
		if (transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
				&& transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) return null;
		if (minecraft.world == null) return null;
		for (Entity entity : minecraft.world.loadedEntityList) {
			if (entity instanceof EntityLivingBase living
					&& (living.getHeldItemMainhand() == stack || living.getHeldItemOffhand() == stack)) return living;
		}
		return null;
	}

	// Held items keep one holder through stack data changes and receive a new holder when re-equipped.
	protected int getHolderCacheKey(ItemStack itemStack, ItemCameraTransforms.TransformType transformType) {
		EntityItemRenderContext renderContext = EntityItemRenderContext.getCurrent();
		if (renderContext == null || renderContext.getEntity() instanceof EntityPlayer) {
			Minecraft minecraft = Minecraft.getMinecraft();
			if (minecraft.player != null && (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
					|| transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)) {
				EnumHandSide renderedHandSide = transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
						? EnumHandSide.RIGHT : EnumHandSide.LEFT;
				EnumHand renderedHand = minecraft.player.getPrimaryHand() == renderedHandSide ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				ItemStack heldStack = minecraft.player.getHeldItem(renderedHand);
				int equipSession = this.updateEquipSession(minecraft.player, renderedHand, heldStack);
				return Objects.hash(minecraft.player.getUniqueID(), renderedHand, transformType, equipSession);
			}

			if (minecraft.world != null) {
				for (EntityPlayer player : minecraft.world.playerEntities) {
					EnumHand heldHand = null;
					if (player.getHeldItemMainhand() == itemStack) heldHand = EnumHand.MAIN_HAND;
					else if (player.getHeldItemOffhand() == itemStack) heldHand = EnumHand.OFF_HAND;

					if (heldHand != null) {
						int equipSession = this.updateEquipSession(player, heldHand, itemStack);
						return Objects.hash(player.getUniqueID(), heldHand, transformType, equipSession);
					}
				}
			}
		}

		// Preserve existing player cache identities for every transform before considering other entities.
		Entity entity = this.getEntityHolder(itemStack, transformType);
		if (entity != null) {
			EnumHand hand = null;
			if (entity instanceof EntityLivingBase living) {
				if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
						|| transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
					EnumHandSide side = transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
							? EnumHandSide.RIGHT : EnumHandSide.LEFT;
					hand = living.getPrimaryHand() == side ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
				}
				else if (living.getHeldItemMainhand() == itemStack) hand = EnumHand.MAIN_HAND;
				else if (living.getHeldItemOffhand() == itemStack) hand = EnumHand.OFF_HAND;
				if (hand != null) {
					int equipSession = this.updateEquipSession(living, hand, itemStack);
					return Objects.hash(entity.getUniqueID(), hand, transformType, equipSession);
				}
			}
			EntityItemRenderContext context = EntityItemRenderContext.getCurrent();
			String attachment = context != null ? context.getAttachment() : null;
			return Objects.hash(entity.getUniqueID(), attachment, transformType, itemStack.getItem());
		}
		return Objects.hash(
				itemStack.getItem(),
				itemStack.getMetadata(),
				itemStack.hasTagCompound() ? itemStack.getTagCompound().toString() : null,
				transformType
		);
	}

	public void updateEquipSessions() {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.world == null) {
			this.equippedItemIdentities.clear();
			this.equipSessions.clear();
			return;
		}

		Set<UUID> currentEntities = new HashSet<>();
		// Keep tracking every player, including entries absent from loadedEntityList.
		for (EntityPlayer player : minecraft.world.playerEntities) {
			currentEntities.add(player.getUniqueID());
			for (EnumHand hand : EnumHand.values()) this.updateEquipSession(player, hand, player.getHeldItem(hand));
		}
		for (Entity entity : minecraft.world.loadedEntityList) {
			if (!(entity instanceof EntityLivingBase living) || entity instanceof EntityPlayer) continue;
			currentEntities.add(living.getUniqueID());
			for (EnumHand hand : EnumHand.values()) {
				ItemStack heldStack = living.getHeldItem(hand);
				this.updateEquipSession(living, hand, heldStack);
			}
		}

		this.equippedItemIdentities.keySet().retainAll(currentEntities);
		this.equipSessions.keySet().retainAll(currentEntities);
	}

	private int updateEquipSession(@NotNull EntityLivingBase entity, @NotNull EnumHand hand, @NotNull ItemStack heldStack) {
		UUID entityId = entity.getUniqueID();
		EnumMap<EnumHand, Integer> equippedItems = this.equippedItemIdentities.computeIfAbsent(entityId, ignored -> new EnumMap<>(EnumHand.class));
		EnumMap<EnumHand, Integer> entityEquipSessions = this.equipSessions.computeIfAbsent(entityId, ignored -> new EnumMap<>(EnumHand.class));

		if (heldStack.isEmpty() || heldStack.getItem().getTileEntityItemStackRenderer() != this) {
			equippedItems.remove(hand);
			return entityEquipSessions.getOrDefault(hand, 0);
		}

		int selectedSlot = -1;
		if (hand == EnumHand.MAIN_HAND && entity instanceof EntityPlayer player) selectedSlot = player.inventory.currentItem;
		int itemIdentity = Objects.hash(heldStack.getItem(), selectedSlot);
		Integer previousIdentity = equippedItems.put(hand, itemIdentity);
		if (!Objects.equals(previousIdentity, itemIdentity)) entityEquipSessions.merge(hand, 1, Integer::sum);
		return entityEquipSessions.getOrDefault(hand, 0);
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

		GeoModel model = this.modelProvider.getModel(animatable);
		this.modelProvider.setClientAnimations(animatable);
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
}
