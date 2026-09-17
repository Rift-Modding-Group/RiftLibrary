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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
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
		return holder;
	}

	//held items keep one holder through stack data changes and receive a new holder when re-equipped
	protected int getHolderCacheKey(ItemStack itemStack, ItemCameraTransforms.TransformType transformType) {
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

		Set<UUID> currentPlayers = new HashSet<>();
		for (EntityPlayer player : minecraft.world.playerEntities) {
			currentPlayers.add(player.getUniqueID());
			for (EnumHand hand : EnumHand.values()) {
				ItemStack heldStack = player.getHeldItem(hand);
				this.updateEquipSession(player, hand, heldStack);
			}
		}

		this.equippedItemIdentities.keySet().retainAll(currentPlayers);
		this.equipSessions.keySet().retainAll(currentPlayers);
	}

	private int updateEquipSession(EntityPlayer player, EnumHand hand, ItemStack heldStack) {
		UUID playerId = player.getUniqueID();
		EnumMap<EnumHand, Integer> equippedItems = this.equippedItemIdentities.computeIfAbsent(playerId, ignored -> new EnumMap<>(EnumHand.class));
		EnumMap<EnumHand, Integer> playerEquipSessions = this.equipSessions.computeIfAbsent(playerId, ignored -> new EnumMap<>(EnumHand.class));

		if (heldStack.isEmpty() || heldStack.getItem().getTileEntityItemStackRenderer() != this) {
			equippedItems.remove(hand);
			return playerEquipSessions.getOrDefault(hand, 0);
		}

		int selectedSlot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : -1;
		int itemIdentity = Objects.hash(heldStack.getItem(), selectedSlot);
		Integer previousIdentity = equippedItems.put(hand, itemIdentity);
		if (!Objects.equals(previousIdentity, itemIdentity)) playerEquipSessions.merge(hand, 1, Integer::sum);
		return playerEquipSessions.getOrDefault(hand, 0);
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
