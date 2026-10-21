package anightdazingzoroark.riftlib.renderers.geo;

import java.util.List;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosSnapshot;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.util.AnimationUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unchecked"})
public abstract class GeoEntityLivingRenderer<A extends EntityLivingBase & IAnimatable<? extends AnimationDataEntityLiving>> extends RenderLivingBase<A> implements IGeoRenderer<A> {
	static {
		AnimationController.addModelFetcher((IAnimatable<?> object) -> {
			if (object instanceof Entity entity) {
				return (IAnimatableModel<Object>) AnimationUtils.getGeoModelForEntity(entity);
			}
			return null;
		});
	}

	protected final AnimatedGeoModel<A> modelProvider;
	protected final List<GeoEntityLivingLayerRenderer<A>> layerRenderers = Lists.newArrayList();
	private float locatorPartialTicks;

	public GeoEntityLivingRenderer(@NotNull RenderManager renderManager, @NotNull AnimatedGeoModel<A> modelProvider) {
		super(renderManager, new ModelBase() {}, 0f);
		this.modelProvider = modelProvider;
		this.mainModel = new GeoEntityLivingModel<>(this);
	}

	@Override
	public void doRender(A entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, this, partialTicks, x, y, z))) return;

        //---get model---
        GeoModel model = this.modelProvider.getModel(entity);

		//---snapshot ride position for dynamic ride position users---
		DynamicRidePosSnapshot ridePosSnapshot = entity instanceof IDynamicRideUser<?> dynamicRideUser && entity.isBeingRidden()
				? dynamicRideUser.ridePosList().snapshot : null;

		//rest is good ol rendering code
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		//here, yaw rotation on the head applies to the entire entity when not ridden
		//and the yaw rotation on the body when ridden because yes
		float trueYaw = Interpolations.lerpYaw(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
		float riddenYaw = entity instanceof IDynamicRideUser<?> dynamicRideUser
				? dynamicRideUser.getRenderYaw(partialTicks)
				: Interpolations.lerpYaw(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
		float finalYaw = entity.isBeingRidden() ? riddenYaw : trueYaw;
		this.applyRotations(entity, finalYaw, partialTicks);

		//---set up client animations and update animated locators---
		if (ridePosSnapshot == null || !ridePosSnapshot.consumeClientAnimationPrepared(entity.world.getTotalWorldTime(), partialTicks)) {
			this.modelProvider.prepareClientAnimationPose(entity);
		}

		//---cache post-locator passenger render positions for this frame---
		if (entity instanceof IDynamicRideUser<?> && entity.isBeingRidden() && ridePosSnapshot != null) {
			ridePosSnapshot.storeSnapshot(partialTicks, finalYaw);
			ridePosSnapshot.renderOriginVec = new Vec3d(
					entity.posX - x,
					entity.posY - y,
					entity.posZ - z
			);
			ridePosSnapshot.cachePassengerRidePositions();
			ridePosSnapshot.restoreSnapshot();
		}

        GlStateManager.pushMatrix();
		float scaleValue = entity.getAnimationData().getScale();
		GlStateManager.scale(scaleValue, scaleValue, scaleValue);
		GlStateManager.translate(0, 0.01f, 0);
		Minecraft.getMinecraft().renderEngine.bindTexture(getEntityTexture(entity));
		Color renderColor = this.getRenderColor(entity, partialTicks);

		boolean flag = this.setDoRenderBrightness(entity, partialTicks);

		if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().player)) {
			this.render(model, entity, partialTicks,
					(float) renderColor.getRed() / 255f,
					(float) renderColor.getGreen() / 255f,
					(float) renderColor.getBlue() / 255f,
					(float) renderColor.getAlpha() / 255f
			);
		}

		if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator()) {
			for (GeoEntityLivingLayerRenderer<A> layerRenderer : this.layerRenderers) {
				layerRenderer.render(entity, partialTicks, 0, renderColor);
			}
			float limbSwingAmount = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
			float limbSwing = entity.limbSwing - entity.limbSwingAmount * (1f - partialTicks);
			float ageInTicks = entity.ticksExisted + partialTicks;
			float netHeadYaw = Interpolations.lerpYaw(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks)
					- Interpolations.lerpYaw(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
			float headPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			for (LayerRenderer<A> layerRenderer : super.layerRenderers) {
				layerRenderer.doRenderLayer(
						entity, limbSwing, limbSwingAmount, partialTicks,
						ageInTicks, netHeadYaw, headPitch, 1f / 16f
				);
			}
		}
		if (entity instanceof EntityLiving entityLiving) {
			Entity leashHolder = entityLiving.getLeashHolder();
			if (leashHolder != null) {
				this.renderLeash(entityLiving, x, y, z, entityYaw, partialTicks);
			}
		}

		if (flag) RenderLivingHurtColor.unset();

		GlStateManager.popMatrix();
		GlStateManager.popMatrix();

		if (ridePosSnapshot != null) {
			GeoEntityRenderHelper.renderPassengers(this.renderManager, entity, ridePosSnapshot, finalYaw, partialTicks);
		}

		MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, this, partialTicks, x, y, z));
	}

	/**
	 * override is just to collect partial ticks for use in
	 * the repositionAnimatedLocators override
	 * */
	@Override
	public void render(GeoModel model, A animatable, float partialTicks, float red, float green, float blue, float alpha) {
		this.locatorPartialTicks = partialTicks;
		IGeoRenderer.super.render(model, animatable, partialTicks, red, green, blue, alpha);
	}

	/**
	 * an override specifically for IDynamicRideUser instances
	 * to make sure that custom camera transforms cannot offset
	 * animated locators
	 * */
	@Override
	public void repositionAnimatedLocators(A animatable) {
		if (!(animatable instanceof IDynamicRideUser<?>)) {
			IGeoRenderer.super.repositionAnimatedLocators(animatable);
			return;
		}
		GeoEntityRenderHelper.repositionAnimatedLocators(animatable, this.locatorPartialTicks);
	}

	/**
	 * Make it so that when rendering an entity, hitboxes being in range of camera also counts.
	 * */
	@Override
	public boolean shouldRender(A livingEntity, ICamera camera, double camX, double camY, double camZ) {
		if ((livingEntity instanceof IDynamicRideUser<?> && livingEntity.isBeingRidden())) return true;

		boolean mainRender = super.shouldRender(livingEntity, camera, camX, camY, camZ);
		if (livingEntity.getParts() == null) return mainRender;

		for (Entity hitboxPart : livingEntity.getParts()) {
			AxisAlignedBB hitboxAABB = hitboxPart.getEntityBoundingBox();
			if (camera.isBoundingBoxInFrustum(hitboxAABB)) return true;
		}

		return mainRender;
	}

	@Override
	public ResourceLocation getEntityTexture(A entity) {
		return this.getTextureLocation(entity);
	}

	@Override
	public GeoModelProvider<A> getGeoModelProvider() {
		return this.modelProvider;
	}

	protected void applyRotations(A entityLiving, float rotationYaw, float partialTicks) {
		//normal model rotation
		GlStateManager.rotate(180f - rotationYaw, 0, 1, 0);
		//rotate pitch while dying
		if (entityLiving.deathTime > 0) {
			float f = ((float) entityLiving.deathTime + partialTicks - 1f) / 20f * 1.6f;
			f = MathHelper.sqrt(f);
			if (f > 1f) f = 1f;
			GlStateManager.rotate(f * this.getDeathMaxRotation(entityLiving), 0, 0, 1);
		}
		//dinnerbone and grumm easter eggs
		else if (entityLiving.hasCustomName()) {
			String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());
			if (("Dinnerbone".equals(s) || "Grumm".equals(s))) {
				GlStateManager.translate(0D, entityLiving.height + 0.1D, 0D);
				GlStateManager.rotate(180, 0, 0, 1);
			}
		}
	}

	@Override
	protected boolean isVisible(A livingEntityIn) {
		return !livingEntityIn.isInvisible();
	}

    public boolean addLayer(GeoEntityLivingLayerRenderer<A> layer) {
		return this.layerRenderers.add(layer);
	}

	@Override
	protected boolean setDoRenderBrightness(A entityLivingBaseIn, float partialTicks) {
		return RenderLivingHurtColor.set(entityLivingBaseIn, partialTicks);
	}

	protected void renderLeash(EntityLiving entityLivingIn, double x, double y, double z, float entityYaw, float partialTicks) {
		Entity entity = entityLivingIn.getLeashHolder();

		if (entity != null) {
			y = y - (1.6D - (double) entityLivingIn.height) * 0.5D;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			double d0 = this.interpolateValue((double) entity.prevRotationYaw, (double) entity.rotationYaw,
					(double) (partialTicks * 0.5F)) * 0.01745329238474369D;
			double d1 = this.interpolateValue((double) entity.prevRotationPitch, (double) entity.rotationPitch,
					(double) (partialTicks * 0.5F)) * 0.01745329238474369D;
			double d2 = Math.cos(d0);
			double d3 = Math.sin(d0);
			double d4 = Math.sin(d1);

			if (entity instanceof EntityHanging) {
				d2 = 0.0D;
				d3 = 0.0D;
				d4 = -1.0D;
			}

			double d5 = Math.cos(d1);
			double d6 = this.interpolateValue(entity.prevPosX, entity.posX, (double) partialTicks) - d2 * 0.7D
					- d3 * 0.5D * d5;
			double d7 = this.interpolateValue(entity.prevPosY + (double) entity.getEyeHeight() * 0.7D,
					entity.posY + (double) entity.getEyeHeight() * 0.7D, (double) partialTicks) - d4 * 0.5D - 0.25D;
			double d8 = this.interpolateValue(entity.prevPosZ, entity.posZ, (double) partialTicks) - d3 * 0.7D
					+ d2 * 0.5D * d5;
			double d9 = this.interpolateValue((double) entityLivingIn.prevRenderYawOffset,
					(double) entityLivingIn.renderYawOffset, (double) partialTicks) * 0.01745329238474369D
					+ (Math.PI / 2D);
			d2 = Math.cos(d9) * (double) entityLivingIn.width * 0.4D;
			d3 = Math.sin(d9) * (double) entityLivingIn.width * 0.4D;
			double d10 = this.interpolateValue(entityLivingIn.prevPosX, entityLivingIn.posX, (double) partialTicks)
					+ d2;
			double d11 = this.interpolateValue(entityLivingIn.prevPosY, entityLivingIn.posY, (double) partialTicks);
			double d12 = this.interpolateValue(entityLivingIn.prevPosZ, entityLivingIn.posZ, (double) partialTicks)
					+ d3;
			x = x + d2;
			z = z + d3;
			double d13 = (double) ((float) (d6 - d10));
			double d14 = (double) ((float) (d7 - d11));
			double d15 = (double) ((float) (d8 - d12));
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();
			bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

			for (int j = 0; j <= 24; ++j) {
				float f = 0.5F;
				float f1 = 0.4F;
				float f2 = 0.3F;

				if (j % 2 == 0) {
					f *= 0.7F;
					f1 *= 0.7F;
					f2 *= 0.7F;
				}

				float f3 = (float) j / 24.0F;
				bufferbuilder
						.pos(x + d13 * (double) f3 + 0.0D,
								y + d14 * (double) (f3 * f3 + f3) * 0.5D
										+ (double) ((24.0F - (float) j) / 18.0F + 0.125F),
								z + d15 * (double) f3)
						.color(f, f1, f2, 1.0F).endVertex();
				bufferbuilder
						.pos(x + d13 * (double) f3 + 0.025D,
								y + d14 * (double) (f3 * f3 + f3) * 0.5D
										+ (double) ((24.0F - (float) j) / 18.0F + 0.125F) + 0.025D,
								z + d15 * (double) f3)
						.color(f, f1, f2, 1.0F).endVertex();
			}

			tessellator.draw();
			bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

			for (int k = 0; k <= 24; ++k) {
				float f4 = 0.5F;
				float f5 = 0.4F;
				float f6 = 0.3F;

				if (k % 2 == 0) {
					f4 *= 0.7F;
					f5 *= 0.7F;
					f6 *= 0.7F;
				}

				float f7 = (float) k / 24.0F;
				bufferbuilder
						.pos(x + d13 * (double) f7 + 0.0D,
								y + d14 * (double) (f7 * f7 + f7) * 0.5D
										+ (double) ((24.0F - (float) k) / 18.0F + 0.125F) + 0.025D,
								z + d15 * (double) f7)
						.color(f4, f5, f6, 1.0F).endVertex();
				bufferbuilder.pos(x + d13 * (double) f7 + 0.025D,
						y + d14 * (double) (f7 * f7 + f7) * 0.5D + (double) ((24.0F - (float) k) / 18.0F + 0.125F),
						z + d15 * (double) f7 + 0.025D).color(f4, f5, f6, 1.0F).endVertex();
			}

			tessellator.draw();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableCull();
		}
	}

	private double interpolateValue(double start, double end, double pct) {
		return start + (end - start) * pct;
	}
}
