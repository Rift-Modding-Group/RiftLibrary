package anightdazingzoroark.riftlib.renderers.geo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

/**
 * This class is a hack to access protected setBrightness() and
 * unsetBrightness() methods in order to apply red tint on the models upon
 * entities getting hurt
 */
public class RenderLivingHurtColor extends RenderLivingBase<EntityLivingBase> {
	/**
	 * Private instance
	 */
	private static RenderLivingHurtColor instance;

	public static RenderLivingHurtColor getInstance() {
		if (instance == null) {
			instance = new RenderLivingHurtColor(Minecraft.getMinecraft().getRenderManager(), null, 0);
		}

		return instance;
	}

	public static boolean set(EntityLivingBase entity, float partialTicks) {
		return getInstance().setBrightness(entity, partialTicks, true);
	}

	public static void unset() {
		getInstance().unsetBrightness();
	}

	public RenderLivingHurtColor(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
		super(renderManagerIn, modelBaseIn, shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLivingBase entity) {
		return null;
	}
}
