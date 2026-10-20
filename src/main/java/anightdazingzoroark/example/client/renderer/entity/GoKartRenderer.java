package anightdazingzoroark.example.client.renderer.entity;

import anightdazingzoroark.example.client.model.entity.GoKartModel;
import anightdazingzoroark.example.entity.GoKartEntity;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityNonLivingRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import org.jetbrains.annotations.NotNull;

public class GoKartRenderer extends GeoEntityNonLivingRenderer<GoKartEntity> {
    public GoKartRenderer(@NotNull RenderManager renderManager) {
        super(renderManager, new GoKartModel());
    }
}
