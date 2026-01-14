package anightdazingzoroark.example.client.renderer.entity;

import anightdazingzoroark.example.client.model.entity.GoKartModel;
import anightdazingzoroark.example.entity.GoKart;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class GoKartRenderer extends GeoEntityRenderer<GoKart> {
    public GoKartRenderer(RenderManager renderManager) {
        super(renderManager, new GoKartModel());
    }
}
