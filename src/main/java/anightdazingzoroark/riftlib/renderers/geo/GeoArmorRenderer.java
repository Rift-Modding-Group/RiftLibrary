package anightdazingzoroark.riftlib.renderers.geo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.apache.commons.lang3.tuple.MutablePair;
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

	private String headBone = "";
    private String bodyBone = "";
    private String rightArmBone = "";
    private String leftArmBone = "";
    private String hipsBone = "";
    private String rightLegBone = "";
    private String leftLegBone = "";
    private String rightBootBone = "";
    private String leftBootBone = "";

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
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		this.render(ageInTicks);
	}

	public void render(float partialTicks) {
		GeoModel model = this.modelProvider.getModel(this.currentArmorHolder);

		GlStateManager.translate(0.0D, 1.501F, 0.0D);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		this.modelProvider.setClientAnimations(this.currentArmorHolder);
		this.modelProvider.createAndUpdateAnimatedLocators(this.currentArmorHolder);
		this.fitToBiped();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.01f, 0);
		IBone rightArmBone = !this.rightArmBone.isEmpty() ? this.modelProvider.getBone(this.rightArmBone) : null;
		IBone leftArmBone = !this.leftArmBone.isEmpty() ? this.modelProvider.getBone(this.leftArmBone) : null;
		if (this.swingProgress > 0.0F) {
            if (rightArmBone != null) {
                rightArmBone.getScale().z = 1.25f;
                rightArmBone.getScale().x = 1.25f;
            }
            if (leftArmBone != null) {
                leftArmBone.getScale().z = 1.3f;
                leftArmBone.getScale().x = 1.05f;
            }
		}
		if (this.isSneak) {
			IBone headBone = !this.headBone.isEmpty() ? this.modelProvider.getBone(this.headBone) : null;
			IBone bodyBone = !this.bodyBone.isEmpty() ? this.modelProvider.getBone(this.bodyBone) : null;
			IBone rightLegBone = !this.rightLegBone.isEmpty() ? this.modelProvider.getBone(this.rightLegBone) : null;
			IBone leftLegBone = !this.leftLegBone.isEmpty() ? this.modelProvider.getBone(this.leftLegBone) : null;
			IBone rightBootBone = !this.rightBootBone.isEmpty() ? this.modelProvider.getBone(this.rightBootBone) : null;
			IBone leftBootBone = !this.leftBootBone.isEmpty() ? this.modelProvider.getBone(this.leftBootBone) : null;

            if (headBone != null) headBone.getPosition().y = headBone.getPosition().y - 3.5f;

            if (bodyBone != null) {
                bodyBone.getPosition().z = bodyBone.getPosition().x - 0.4f;
                bodyBone.getPosition().y = bodyBone.getPosition().x - 3.5f;
            }

            if (rightArmBone != null && bodyBone != null) {
                rightArmBone.getPosition().y = bodyBone.getPosition().x - 3f;
                rightArmBone.getPosition().x = bodyBone.getPosition().x + 0.35f;
            }

            if (leftArmBone != null && bodyBone != null) {
                leftArmBone.getPosition().y = bodyBone.getPosition().x - 3f;
                leftArmBone.getPosition().x = bodyBone.getPosition().x - 0.35f;
            }

            if (rightLegBone != null && bodyBone != null) {
                rightLegBone.getPosition().z = bodyBone.getPosition().x + 4f;
            }

            if (leftLegBone != null && bodyBone != null) {
                leftLegBone.getPosition().z = bodyBone.getPosition().x + 4f;
            }

            if (rightBootBone != null && bodyBone != null) {
                rightBootBone.getPosition().z = bodyBone.getPosition().x + 4f;
            }

            if (leftBootBone != null && bodyBone != null) {
                leftBootBone.getPosition().z = bodyBone.getPosition().x + 4f;
            }
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
        this.tryFitBoneToBiped(this.bipedHead, this.headBone);
        this.tryFitBoneToBiped(this.bipedBody, this.bodyBone);
        this.tryFitBoneToBiped(this.bipedRightArm, this.rightArmBone);
        this.tryFitBoneToBiped(this.bipedLeftArm, this.leftArmBone);
        this.tryFitBoneToBiped(this.bipedBody, this.hipsBone);
        this.tryFitBoneToBiped(this.bipedRightLeg, this.rightLegBone);
        this.tryFitBoneToBiped(this.bipedLeftLeg, this.leftLegBone);
        this.tryFitBoneToBiped(this.bipedRightLeg, this.rightBootBone);
        this.tryFitBoneToBiped(this.bipedLeftLeg, this.leftBootBone);
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

        this.tryHideBone(this.headBone, true);
        this.tryHideBone(this.bodyBone, true);
        this.tryHideBone(this.rightArmBone, true);
        this.tryHideBone(this.leftArmBone, true);
        this.tryHideBone(this.hipsBone, true);
        this.tryHideBone(this.rightLegBone, true);
        this.tryHideBone(this.leftLegBone, true);
        this.tryHideBone(this.rightBootBone, true);
        this.tryHideBone(this.leftBootBone, true);

        switch (slot) {
            case HEAD:
                this.tryHideBone(this.headBone, false);
                break;
            case CHEST:
                this.tryHideBone(this.bodyBone, false);
                this.tryHideBone(this.rightArmBone, false);
                this.tryHideBone(this.leftArmBone, false);
                break;
            case LEGS:
                this.tryHideBone(this.hipsBone, false);
                this.tryHideBone(this.rightLegBone, false);
                this.tryHideBone(this.leftLegBone, false);
                break;
            case FEET:
                this.tryHideBone(this.rightBootBone, false);
                this.tryHideBone(this.leftBootBone, false);
                break;
        }
		return this;
	}

    private void tryHideBone(String boneName, boolean value) {
        if (boneName.isEmpty()) return;
        IBone boneToHide = this.modelProvider.getBone(boneName);
        if (boneToHide != null) boneToHide.setHidden(value);
    }

    //setting bones here
    public void setHeadBone(String name) {
        if (name == null) RiftLib.LOGGER.warn("Cannot assign null as bone name");
        this.headBone = name;
    }

    public void setBodyBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.bodyBone = name;
    }

    public void setRightArmBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.rightArmBone = name;
    }

    public void setLeftArmBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.leftArmBone = name;
    }

    public void setHipsBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.hipsBone = name;
    }

    public void setRightLegBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.rightLegBone = name;
    }

    public void setLeftLegBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.leftLegBone = name;
    }

    public void setRightBootBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.rightBootBone = name;
    }

    public void setLeftBootBone(String name) {
        if (name == null) {
            RiftLib.LOGGER.warn("Cannot assign null as bone name");
            return;
        }
        this.leftBootBone = name;
    }
}
