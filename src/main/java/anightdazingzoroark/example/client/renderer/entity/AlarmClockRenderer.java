package anightdazingzoroark.example.client.renderer.entity;

import anightdazingzoroark.example.client.model.entity.AlarmClockModel;
import anightdazingzoroark.example.entity.AlarmClockEntity;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class AlarmClockRenderer extends GeoEntityRenderer<AlarmClockEntity> {
    public AlarmClockRenderer(RenderManager renderManager) {
        super(renderManager, new AlarmClockModel());
    }
}
