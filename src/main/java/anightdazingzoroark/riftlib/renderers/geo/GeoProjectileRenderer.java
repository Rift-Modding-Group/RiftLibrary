package anightdazingzoroark.riftlib.renderers.geo;

import java.util.Collections;

import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.model.provider.data.EntityModelData;
import anightdazingzoroark.riftlib.util.AnimationUtils;

@SuppressWarnings("unchecked")
public class GeoProjectileRenderer<T extends RiftLibProjectile & IAnimatable> extends Render<T> implements IGeoRenderer<T> {
	static {
		AnimationController.addModelFetcher((IAnimatable object) -> {
			if (object instanceof RiftLibProjectile) {
				return (IAnimatableModel<Object>) AnimationUtils.getGeoModelForEntity((RiftLibProjectile) object);
			}
			return null;
		});
	}

	private final AnimatedGeoModel<T> modelProvider;

	public GeoProjectileRenderer(RenderManager renderManager, AnimatedGeoModel<T> modelProvider) {
		super(renderManager);
		this.modelProvider = modelProvider;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(entity));
        Integer uniqueID = this.getUniqueID(entity);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(
				entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 180f,
				0f, 1f, 0f
		);
		if (entity.canRotateVertically()) {
			GlStateManager.rotate(
					entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
					1f, 0f, 0
			);
		}

		EntityModelData entityModelData = new EntityModelData();
		//a projectile not on the ground is usually on the move, whether its from gravity or from following
		//a trajectory
		AnimationEvent<T> predicate = new AnimationEvent<T>(entity, 0, 0, partialTicks,
				!entity.onGround, Collections.singletonList(entityModelData));

        this.modelProvider.setLivingAnimations(entity, uniqueID, predicate);
		this.modelProvider.createAndUpdateAnimatedLocators(entity, uniqueID);

        GlStateManager.pushMatrix();
		GlStateManager.scale(entity.scale(), entity.scale(), entity.scale());
		Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(entity));
		Color renderColor = this.getRenderColor(entity, partialTicks);

		if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().player)) {
			this.render(
					model, entity, partialTicks,
					(float) renderColor.getRed() / 255f,
					(float) renderColor.getGreen() / 255f,
					(float) renderColor.getBlue() / 255f,
					(float) renderColor.getAlpha() / 255f
			);
		}
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}

	@Override
	public GeoModelProvider<T> getGeoModelProvider() {
		return this.modelProvider;
	}

	@Override
	public ResourceLocation getEntityTexture(T instance) {
		return this.getTextureLocation(instance);
	}

	@Override
	public Integer getUniqueID(T animatable) {
		return animatable.getUniqueID().hashCode();
	}

	@Override
	public ResourceLocation getTextureLocation(T instance) {
		return this.modelProvider.getTextureLocation(instance);
	}
}
