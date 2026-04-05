package anightdazingzoroark.riftlib.renderers.geo;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;

public abstract class GeoLayerRenderer<T extends EntityLivingBase & IAnimatable> implements LayerRenderer<T> {
	protected final IGeoRenderer<T> entityRenderer;

	public GeoLayerRenderer(IGeoRenderer<T> entityRendererIn) {
		this.entityRenderer = entityRendererIn;
	}

	protected static <T extends EntityLivingBase> void renderCopyCutoutModel(
			ModelBase modelParentIn, ModelBase modelIn,
			T entityIn, float ageInTicks,
			float partialTicks, float red, float green, float blue
	) {
		if (entityIn.isInvisible()) return;
		modelParentIn.setModelAttributes(modelIn);
		modelIn.setLivingAnimations(entityIn, 0, 0, partialTicks);
		modelIn.setRotationAngles(0, 0, ageInTicks, 0, 0, 1 / 16f, entityIn);
		renderCutoutModel(
				modelIn, entityIn, ageInTicks,
				1 / 16f, red, green, blue
		);
	}

	protected static <T extends EntityLivingBase> void renderCutoutModel(
			ModelBase modelIn, T entityIn, float ageInTicks, float scale,
			float red, float green, float blue
	) {
		GlStateManager.color(red, green, blue, 1f);
		modelIn.render(entityIn, 0, 0, ageInTicks, 0, 0, scale);
	}

	@Override
	public void doRenderLayer(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {}

	public IGeoRenderer<T> getRenderer() {
		return this.entityRenderer;
	}

	@SuppressWarnings("unchecked")
	public GeoModelProvider<T> getEntityModel() {
		return this.entityRenderer.getGeoModelProvider();
	}

	protected ResourceLocation getEntityTexture(T entityIn) {
		return this.entityRenderer.getTextureLocation(entityIn);
	}

	public abstract void render(T entitylivingbaseIn, float partialTicks, float ageInTicks, Color renderColor);
}
