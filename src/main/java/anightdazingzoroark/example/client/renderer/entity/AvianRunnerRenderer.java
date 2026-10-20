package anightdazingzoroark.example.client.renderer.entity;

import anightdazingzoroark.example.client.model.entity.AvianRunnerModel;
import anightdazingzoroark.example.entity.AvianRunnerEntity;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityLivingRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class AvianRunnerRenderer extends GeoEntityLivingRenderer<AvianRunnerEntity> {
    public AvianRunnerRenderer(RenderManager renderManager) {
        super(renderManager, new AvianRunnerModel());
    }
}
