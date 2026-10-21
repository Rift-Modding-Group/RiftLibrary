package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.geo.GeoModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.BufferUtils;
import org.lwjglx.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * Adapts a geo entity model to vanilla's {@link ModelBase} layer API.
 */
public class GeoEntityLivingModel<A extends EntityLivingBase & IAnimatable<? extends AnimationDataEntityLiving>> extends ModelBase {
    @NotNull
    private final GeoEntityLivingRenderer<A> renderer;
    @NotNull
    private final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4);

    public GeoEntityLivingModel(@NotNull GeoEntityLivingRenderer<A> renderer) {
        this.renderer = renderer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(
            @NotNull Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            float scale
    ) {
        A animatable = (A) entity;
        GeoModel model = this.renderer.modelProvider.getModel(animatable);
        this.colorBuffer.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, this.colorBuffer);
        this.renderer.renderModel(
                model,
                this.colorBuffer.get(0),
                this.colorBuffer.get(1),
                this.colorBuffer.get(2),
                this.colorBuffer.get(3)
        );
    }
}
