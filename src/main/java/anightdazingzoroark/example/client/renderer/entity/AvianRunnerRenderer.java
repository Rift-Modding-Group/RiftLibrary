package anightdazingzoroark.example.client.renderer.entity;

import anightdazingzoroark.example.client.model.entity.AvianRunnerModel;
import anightdazingzoroark.example.entity.AvianRunnerEntity;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class AvianRunnerRenderer extends GeoEntityRenderer<AvianRunnerEntity> {
    public AvianRunnerRenderer(RenderManager renderManager) {
        super(renderManager, new AvianRunnerModel());
    }
}
