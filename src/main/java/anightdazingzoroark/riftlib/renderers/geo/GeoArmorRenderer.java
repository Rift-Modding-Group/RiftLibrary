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
	public String headBone = "armorHead";
	public String bodyBone = "armorBody";
	public String rightArmBone = "armorRightArm";
	public String leftArmBone = "armorLeftArm";
    public String hipsBone = "armorHipsBone";
	public String rightLegBone = "armorRightLeg";
	public String leftLegBone = "armorLeftLeg";
	public String rightBootBone = "armorRightBoot";
	public String leftBootBone = "armorLeftBoot";

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
		IBone rightArmBone = this.modelProvider.getBone(this.rightArmBone);
		IBone leftArmBone = this.modelProvider.getBone(this.leftArmBone);
		if (this.swingProgress > 0.0F) {
			rightArmBone.setScaleZ(1.25f);
			rightArmBone.setScaleX(1.25f);
			leftArmBone.setScaleZ(1.3f);
			leftArmBone.setScaleX(1.05f);
		}
		if (isSneak) {
			IBone headBone = this.modelProvider.getBone(this.headBone);
			IBone bodyBone = this.modelProvider.getBone(this.bodyBone);
			IBone rightLegBone = this.modelProvider.getBone(this.rightLegBone);
			IBone leftLegBone = this.modelProvider.getBone(this.leftLegBone);
			IBone rightBootBone = this.modelProvider.getBone(this.rightBootBone);
			IBone leftBootBone = this.modelProvider.getBone(this.leftBootBone);

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
		Minecraft.getMinecraft().renderEngine.bindTexture(getTextureLocation(currentArmorItem));
		Color renderColor = getRenderColor(currentArmorItem, partialTicks);
		render(model, currentArmorItem, partialTicks,
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
        IBone boneToHide = this.modelProvider.getBone(boneName);
        if (boneToHide != null) boneToHide.setHidden(value);
    }

	@Override
	public Integer getUniqueID(T animatable) {
		return Objects.hash(this.armorSlot, itemStack.getItem(), itemStack.getCount(),
				itemStack.hasTagCompound() ? itemStack.getTagCompound().toString() : 1,
				this.entityLiving.getUniqueID().toString());
	}
}
