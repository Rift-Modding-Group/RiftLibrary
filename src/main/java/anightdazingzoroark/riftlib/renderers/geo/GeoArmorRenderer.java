package anightdazingzoroark.riftlib.renderers.geo;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import anightdazingzoroark.riftlib.RiftLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.util.GeoUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class GeoArmorRenderer<T extends ItemArmor & IAnimatable> extends ModelBiped
		implements IGeoRenderer<T> {
	private static Map<Class<? extends ItemArmor>, GeoArmorRenderer> renderers = new ConcurrentHashMap<>();

	static {
		AnimationController.addModelFetcher((IAnimatable object) -> {
			if (object instanceof ItemArmor) {
				GeoArmorRenderer renderer = renderers.get(object.getClass());
				return renderer == null ? null : renderer.getGeoModelProvider();
			}
			return null;
		});
	}

	private T currentArmorItem;
	private EntityLivingBase entityLiving;
	private ItemStack itemStack;
	private EntityEquipmentSlot armorSlot;

	// Set these to the names of your armor's bones
	private String headBone = "";
    private String bodyBone = "";
    private String rightArmBone = "";
    private String leftArmBone = "";
    private String hipsBone = "";
    private String rightLegBone = "";
    private String leftLegBone = "";
    private String rightBootBone = "";
    private String leftBootBone = "";

	public static void registerArmorRenderer(Class<? extends ItemArmor> itemClass, GeoArmorRenderer renderer) {
		renderers.put(itemClass, renderer);
	}

	public static GeoArmorRenderer getRenderer(Class<? extends ItemArmor> item) {
		return renderers.get(item);
	}

	private final AnimatedGeoModel<T> modelProvider;

	public GeoArmorRenderer(AnimatedGeoModel<T> modelProvider) {
		super(1);
		this.modelProvider = modelProvider;
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		this.render(ageInTicks);
	}

	public void render(float partialTicks) {
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(this.currentArmorItem));
        Integer uniqueID = this.getUniqueID(this.currentArmorItem);

		GlStateManager.translate(0.0D, 1.501F, 0.0D);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		AnimationEvent itemEvent = new AnimationEvent(this.currentArmorItem, 0, 0, 0, false,
				Arrays.asList(this.itemStack, this.entityLiving, this.armorSlot));
		modelProvider.setLivingAnimations(currentArmorItem, uniqueID, itemEvent);
		this.fitToBiped();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0.01f, 0);
		IBone rightArmBone = !this.rightArmBone.isEmpty() ? this.modelProvider.getBone(this.rightArmBone) : null;
		IBone leftArmBone = !this.leftArmBone.isEmpty() ? this.modelProvider.getBone(this.leftArmBone) : null;
		if (this.swingProgress > 0.0F) {
            if (rightArmBone != null) {
                rightArmBone.setScaleZ(1.25f);
                rightArmBone.setScaleX(1.25f);
            }
            if (leftArmBone != null) {
                leftArmBone.setScaleZ(1.3f);
                leftArmBone.setScaleX(1.05f);
            }
		}
		if (this.isSneak) {
			IBone headBone = !this.headBone.isEmpty() ? this.modelProvider.getBone(this.headBone) : null;
			IBone bodyBone = !this.bodyBone.isEmpty() ? this.modelProvider.getBone(this.bodyBone) : null;
			IBone rightLegBone = !this.rightLegBone.isEmpty() ? this.modelProvider.getBone(this.rightLegBone) : null;
			IBone leftLegBone = !this.leftLegBone.isEmpty() ? this.modelProvider.getBone(this.leftLegBone) : null;
			IBone rightBootBone = !this.rightBootBone.isEmpty() ? this.modelProvider.getBone(this.rightBootBone) : null;
			IBone leftBootBone = !this.leftBootBone.isEmpty() ? this.modelProvider.getBone(this.leftBootBone) : null;

            if (headBone != null) headBone.setPositionY(headBone.getPositionY() - 3.5f);

            if (bodyBone != null) {
                bodyBone.setPositionZ(bodyBone.getPositionX() - 0.4f);
                bodyBone.setPositionY(bodyBone.getPositionX() - 3.5f);
            }

            if (rightArmBone != null && bodyBone != null) {
                rightArmBone.setPositionY(bodyBone.getPositionX() - 3);
                rightArmBone.setPositionX(bodyBone.getPositionX() + 0.35f);
            }

            if (leftArmBone != null && bodyBone != null) {
                leftArmBone.setPositionY(bodyBone.getPositionX() - 3);
                leftArmBone.setPositionX(bodyBone.getPositionX() - 0.35f);
            }

            if (rightLegBone != null && bodyBone != null) {
                rightLegBone.setPositionZ(bodyBone.getPositionX() + 4);
            }

            if (leftLegBone != null && bodyBone != null) {
                leftLegBone.setPositionZ(bodyBone.getPositionX() + 4);
            }

            if (rightBootBone != null && bodyBone != null) {
                rightBootBone.setPositionZ(bodyBone.getPositionX() + 4);
            }

            if (leftBootBone != null && bodyBone != null) {
                leftBootBone.setPositionZ(bodyBone.getPositionX() + 4);
            }
		}
		Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(this.currentArmorItem));
		Color renderColor = this.getRenderColor(this.currentArmorItem, partialTicks);
		render(model, this.currentArmorItem, partialTicks,
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
        if (boneToFit != null) GeoUtils.copyRotations(bipedBone, boneToFit);
    }

	@Override
	public AnimatedGeoModel<T> getGeoModelProvider() {
		return this.modelProvider;
	}

	@Override
	public ResourceLocation getTextureLocation(T instance) {
		return this.modelProvider.getTextureLocation(instance);
	}

	/**
	 * Everything after this point needs to be called every frame before rendering
	 */
	public void setCurrentItem(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot) {
		this.entityLiving = entityLiving;
		this.itemStack = itemStack;
		this.armorSlot = armorSlot;
		this.currentArmorItem = (T) itemStack.getItem();
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
		this.modelProvider.getModel(this.modelProvider.getModelLocation(this.currentArmorItem));

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

	@Override
	public Integer getUniqueID(T animatable) {
		return Objects.hash(
                this.armorSlot,
                this.itemStack.getItem(),
                this.itemStack.getCount(),
				this.itemStack.hasTagCompound() ? this.itemStack.getTagCompound().toString() : 1,
				this.entityLiving.getUniqueID().toString()
        );
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
