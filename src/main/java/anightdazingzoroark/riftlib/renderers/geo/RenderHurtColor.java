package anightdazingzoroark.riftlib.renderers.geo;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link RenderLivingHurtColor} for living entities.
 */
@Deprecated(since = "2.1.0")
public class RenderHurtColor extends RenderLivingHurtColor {
    public RenderHurtColor(@NotNull RenderManager renderManager, @Nullable ModelBase model, float shadowSize) {
        super(renderManager, model, shadowSize);
    }
}
