package anightdazingzoroark.riftlibrary.main.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderHurtColor extends RenderLivingBase<EntityLivingBase> {
    /**
     * Private instance
     */
    private static RenderHurtColor instance;

    public static RenderHurtColor getInstance() {
        if (instance == null) {
            instance = new RenderHurtColor(Minecraft.getMinecraft().getRenderManager(), null, 0);
        }

        return instance;
    }

    public static boolean set(EntityLivingBase entity, float partialTicks) {
        return getInstance().setBrightness(entity, partialTicks, true);
    }

    public static void unset() {
        getInstance().unsetBrightness();
    }

    public RenderHurtColor(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLivingBase entity) {
        return null;
    }
}
