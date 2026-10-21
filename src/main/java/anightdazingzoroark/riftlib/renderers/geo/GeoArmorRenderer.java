package anightdazingzoroark.riftlib.renderers.geo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.armor.AnimatedArmorHolder;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class GeoArmorRenderer<T extends AnimatedArmorHolder> extends ModelBiped implements IGeoRenderer<T> {
	private static final long HOLDER_CACHE_TTL_MS = 10000L;
	private static final long HOLDER_CACHE_CLEANUP_INTERVAL_MS = 1000L;
	private static final int HOLDER_CACHE_MAX_SIZE = 256;

	private static final Map<Item, GeoArmorRenderer<?>> renderers = new ConcurrentHashMap<>();

	static {
		AnimationController.addModelFetcher((IAnimatable<?> object) -> {
			if (object instanceof AnimatedArmorHolder holder && holder.getStack().getItem() instanceof ItemArmor armor) {
				GeoArmorRenderer<?> renderer = getRenderer(armor);
				return renderer == null ? null : (IAnimatableModel<Object>) renderer.getGeoModelProvider();
			}
			return null;
		});
	}

	private T currentArmorHolder;
	private EntityLivingBase entityLiving;

    //this defines which bones on the model should be shown or hidden
    //based on the presence of an armor slot
    @NotNull
    private final Map<EntityEquipmentSlot, List<String>> equipmentBoneBindings = new EnumMap<>(EntityEquipmentSlot.class);
    //this defines a bone on the model that corresponds to an armor part for animation
    @NotNull
    private final Map<ArmorBodyPart, List<String>> bodyPartBoneBindings = new EnumMap<>(ArmorBodyPart.class);
    //for legacy bone bindings, which will be removed soon
    @NotNull
    private final Map<ArmorBodyPart, String> legacyBoneBindings = new EnumMap<>(ArmorBodyPart.class);

    //-----static registry stuff-----
	public static void registerArmorRenderer(Item armorItem, GeoArmorRenderer<?> renderer) {
		renderers.put(armorItem, renderer);
	}

    @Nullable
	public static GeoArmorRenderer<?> getRenderer(ItemArmor item) {
		return renderers.get(item);
	}

    //-----for all renderers-----
	private final AnimatedGeoModel<T> modelProvider;
	private final Function<ItemStack, T> holderCreator;
	private final Map<Integer, MutablePair<T, Long>> holderCache = new HashMap<>();
	private long lastHolderCacheCleanup;

	public GeoArmorRenderer(AnimatedGeoModel<T> modelProvider, Function<ItemStack, T> holderCreator) {
		super(1);
		this.modelProvider = modelProvider;
		this.holderCreator = holderCreator;
		GeoItemRendererTicker.ARMOR_RENDERERS.add(this);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		this.render(ageInTicks);
	}

	public void render(float partialTicks) {
		GeoModel model = this.modelProvider.getModel(this.currentArmorHolder);

		GlStateManager.translate(0.0D, 1.501f, 0.0D);
		GlStateManager.scale(-1f, -1f, 1f);

		this.modelProvider.setClientAnimations(this.currentArmorHolder);
		this.modelProvider.createAndUpdateAnimatedLocators(this.currentArmorHolder);
		this.fitToBiped();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.01f, 0);

        //displace for arm swinging
		if (this.swingProgress > 0.0F) {
			this.transformBoundBones(ArmorBodyPart.RIGHT_ARM, bone -> {
				bone.getScale().z = 1.25f;
				bone.getScale().x = 1.25f;
			});
			this.transformBoundBones(ArmorBodyPart.LEFT_ARM, bone -> {
				bone.getScale().z = 1.3f;
				bone.getScale().x = 1.05f;
			});
		}

        //displace for sneaking
		if (this.isSneak) {
			this.transformBoundBones(ArmorBodyPart.HEAD, bone ->
					bone.getPosition().y = bone.getPosition().y - 3.5f
            );

			Consumer<IBone> torsoDisplacement = bone -> {
				bone.getPosition().z = bone.getPosition().z - 0.4f;
				bone.getPosition().y = bone.getPosition().y - 3.5f;
			};
			this.transformBoundBones(ArmorBodyPart.BODY, torsoDisplacement);
			this.transformBoundBones(ArmorBodyPart.HIPS, torsoDisplacement);

			this.transformBoundBones(ArmorBodyPart.RIGHT_ARM, bone -> {
				bone.getPosition().y = bone.getPosition().y - 3f;
				bone.getPosition().x = bone.getPosition().x + 0.35f;
            });

			this.transformBoundBones(ArmorBodyPart.LEFT_ARM, bone -> {
				bone.getPosition().y = bone.getPosition().y - 3f;
				bone.getPosition().x = bone.getPosition().x - 0.35f;
            });

			Consumer<IBone> legDisplacement = bone -> bone.getPosition().z = bone.getPosition().z + 4f;
			this.transformBoundBones(ArmorBodyPart.RIGHT_LEG, legDisplacement);
			this.transformBoundBones(ArmorBodyPart.LEFT_LEG, legDisplacement);
			this.transformBoundBones(ArmorBodyPart.RIGHT_FOOT, legDisplacement);
			this.transformBoundBones(ArmorBodyPart.LEFT_FOOT, legDisplacement);
		}
		Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(this.currentArmorHolder));
		Color renderColor = this.getRenderColor(this.currentArmorHolder, partialTicks);
		render(model, this.currentArmorHolder, partialTicks,
                (float) renderColor.getRed() / 255f,
				(float) renderColor.getGreen() / 255f, (float) renderColor.getBlue() / 255f,
				(float) renderColor.getAlpha() / 255);
		GlStateManager.popMatrix();
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		GlStateManager.translate(0.0D, -1.501F, 0.0D);
	}

	private void fitToBiped() {
        if (this.entityLiving instanceof EntityArmorStand) return;

        for (ArmorBodyPart armorBodyPart : ArmorBodyPart.values()) {
            for (String boundBone : this.getBoundBones(armorBodyPart)) {
                this.tryFitBoneToBiped(armorBodyPart.bipedBoneGetter.apply(this), boundBone);
            }
        }
	}

	private void transformBoundBones(@NotNull ArmorBodyPart armorBodyPart, @NotNull Consumer<IBone> transformation) {
		for (String boundBone : this.getBoundBones(armorBodyPart)) {
			this.transformBoneAndChildren(this.modelProvider.getBone(boundBone), transformation);
		}
	}

    private void transformBoneAndChildren(@Nullable IBone bone, Consumer<IBone> transformation) {
        if (bone == null) return;

        float originalX = bone.getPosition().x;
        float originalY = bone.getPosition().y;
        transformation.accept(bone);

        if (bone instanceof GeoBone geoBone) {
            float childXCompensation = originalX - bone.getPosition().x;
            float childYCompensation = (originalY - bone.getPosition().y) * -0.25f;
            for (GeoBone childBone : geoBone.childBones) {
                childBone.getPosition().x += childXCompensation;
                childBone.getPosition().y += childYCompensation;
            }
        }
    }

    private void tryFitBoneToBiped(ModelRenderer bipedBone, String boneName) {
        if (bipedBone == null) RiftLib.LOGGER.warn("Biped bone to fit to cannot be null");
        if (boneName.isEmpty()) return;
        IBone boneToFit = this.modelProvider.getBone(boneName);
        if (boneToFit != null) boneToFit.getRotation().set(
                -bipedBone.rotateAngleX,
                -bipedBone.rotateAngleY,
                bipedBone.rotateAngleZ
        );
    }

	@Override
	public AnimatedGeoModel<T> getGeoModelProvider() {
		return this.modelProvider;
	}

	public ResourceLocation getArmorTexture(ItemStack stack) {
		T holder = this.currentArmorHolder;
		if (holder == null || !ItemStack.areItemsEqual(holder.getStack(), stack)) {
			holder = this.holderCreator.apply(stack.copy());
		}
		return this.getTextureLocation(holder);
	}

	public ResourceLocation getArmorTexture(EntityLivingBase wearer, ItemStack stack, EntityEquipmentSlot slot) {
		return this.getTextureLocation(this.getOrCreateHolder(wearer, stack, slot));
	}

	/**
	 * Everything after this point needs to be called every frame before rendering
	 */
	public void setCurrentItem(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot) {
		this.entityLiving = entityLiving;
		this.currentArmorHolder = this.getOrCreateHolder(entityLiving, itemStack, armorSlot);
	}

	protected T getOrCreateHolder(EntityLivingBase wearer, ItemStack itemStack, EntityEquipmentSlot armorSlot) {
		long now = Minecraft.getSystemTime();
		this.cleanupHolderCache(now);

		int key = this.getHolderCacheKey(wearer, armorSlot);
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

		holder.getAnimationData().setRenderContext(wearer, holder.getStack(), armorSlot);
		return holder;
	}

	protected int getHolderCacheKey(EntityLivingBase wearer, EntityEquipmentSlot armorSlot) {
		return Objects.hash(wearer.getUniqueID(), armorSlot);
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

	public final GeoArmorRenderer applyEntityStats(ModelBiped defaultArmor) {
		this.isChild = defaultArmor.isChild;
		this.isSneak = defaultArmor.isSneak;
		this.isRiding = defaultArmor.isRiding;
		this.rightArmPose = defaultArmor.rightArmPose;
		this.leftArmPose = defaultArmor.leftArmPose;
		return this;
	}

	@SuppressWarnings("incomplete-switch")
	public GeoArmorRenderer applySlot(EntityEquipmentSlot slot) {
		this.modelProvider.getModel(this.currentArmorHolder);

        //hide everything first
        for (EntityEquipmentSlot equipmentSlot : EntityEquipmentSlot.values()) {
            for (String boundBone : this.getBoundBones(equipmentSlot)) this.tryHideBone(boundBone, true);
        }

        //reveal based on if given slot was equipped
        for (String boundBone : this.getBoundBones(slot)) this.tryHideBone(boundBone, false);
		return this;
	}

    private void tryHideBone(String boneName, boolean value) {
        if (boneName.isEmpty()) return;
        IBone boneToHide = this.modelProvider.getBone(boneName);
        if (boneToHide != null) boneToHide.setHidden(value);
    }

    //setting bones here
    public void bindBoneToEquipmentSlot(@NotNull EntityEquipmentSlot equipmentSlot, @NotNull String name) {
        List<String> boundBones = this.equipmentBoneBindings.computeIfAbsent(equipmentSlot, bodyPart -> new ArrayList<>());
        if (!boundBones.contains(name)) boundBones.add(name);
    }

    public void bindBoneToBodyPart(@NotNull ArmorBodyPart armorBodyPart, @NotNull String name) {
        List<String> boundBones = this.bodyPartBoneBindings.computeIfAbsent(armorBodyPart, bodyPart -> new ArrayList<>());
        if (!boundBones.contains(name)) boundBones.add(name);
    }

	//-----deprecated legacy features-----
    @NotNull
    private List<String> getBoundBones(@NotNull ArmorBodyPart armorBodyPart) {
        List<String> boundBones = this.bodyPartBoneBindings.getOrDefault(armorBodyPart, List.of());
        String legacyBone = this.legacyBoneBindings.get(armorBodyPart);
        if (legacyBone == null || boundBones.contains(legacyBone)) return boundBones;

        List<String> combinedBones = new ArrayList<>(boundBones);
        combinedBones.add(legacyBone);
        return combinedBones;
    }

    @NotNull
    private List<String> getBoundBones(@NotNull EntityEquipmentSlot equipmentSlot) {
        List<String> boundBones = new ArrayList<>(this.equipmentBoneBindings.getOrDefault(equipmentSlot, List.of()));
        for (Map.Entry<ArmorBodyPart, String> legacyBinding : this.legacyBoneBindings.entrySet()) {
            if (legacyBinding.getKey().legacyEquipmentSlot == equipmentSlot && !boundBones.contains(legacyBinding.getValue())) {
                boundBones.add(legacyBinding.getValue());
            }
        }
        return boundBones;
    }

    private void setLegacyBone(@NotNull ArmorBodyPart armorBodyPart, @Nullable String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        if (name.isEmpty()) this.legacyBoneBindings.remove(armorBodyPart);
        else this.legacyBoneBindings.put(armorBodyPart, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setHeadBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.HEAD, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setBodyBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.BODY, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setRightArmBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.RIGHT_ARM, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setLeftArmBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.LEFT_ARM, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setHipsBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.HIPS, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setRightLegBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.RIGHT_LEG, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setLeftLegBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.LEFT_LEG, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setRightBootBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.RIGHT_FOOT, name);
    }

    /**
     * @deprecated Use {@link #bindBoneToBodyPart} and {@link #bindBoneToEquipmentSlot}.
     */
    @Deprecated(since = "2.1.0")
    public void setLeftBootBone(@Nullable String name) {
        this.setLegacyBone(ArmorBodyPart.LEFT_FOOT, name);
    }

    public enum ArmorBodyPart {
        HEAD(bipedModel -> bipedModel.bipedHead, EntityEquipmentSlot.HEAD),
        BODY(bipedModel -> bipedModel.bipedBody, EntityEquipmentSlot.CHEST),
        RIGHT_ARM(bipedModel -> bipedModel.bipedRightArm, EntityEquipmentSlot.CHEST),
        LEFT_ARM(bipedModel -> bipedModel.bipedLeftArm, EntityEquipmentSlot.CHEST),
        HIPS(bipedModel -> bipedModel.bipedBody, EntityEquipmentSlot.LEGS),
        RIGHT_LEG(bipedModel -> bipedModel.bipedRightLeg, EntityEquipmentSlot.LEGS),
        LEFT_LEG(bipedModel -> bipedModel.bipedLeftLeg, EntityEquipmentSlot.LEGS),
        RIGHT_FOOT(bipedModel -> bipedModel.bipedRightLeg, EntityEquipmentSlot.FEET),
        LEFT_FOOT(bipedModel -> bipedModel.bipedLeftLeg, EntityEquipmentSlot.FEET);

        @NotNull
        private final Function<ModelBiped, ModelRenderer> bipedBoneGetter;
        @NotNull
        private final EntityEquipmentSlot legacyEquipmentSlot;

        ArmorBodyPart(@NotNull Function<ModelBiped, ModelRenderer> bipedBoneGetter, @NotNull EntityEquipmentSlot legacyEquipmentSlot) {
            this.bipedBoneGetter = bipedBoneGetter;
            this.legacyEquipmentSlot = legacyEquipmentSlot;
        }
    }
}
