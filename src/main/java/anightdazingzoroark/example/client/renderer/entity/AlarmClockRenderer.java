package anightdazingzoroark.example.client.renderer.entity;

import anightdazingzoroark.example.client.model.entity.AlarmClockModel;
import anightdazingzoroark.example.entity.AlarmClockEntity;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityNonLivingRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import org.jetbrains.annotations.NotNull;

public class AlarmClockRenderer extends GeoEntityNonLivingRenderer<AlarmClockEntity> {
    public AlarmClockRenderer(@NotNull RenderManager renderManager) {
        super(renderManager, new AlarmClockModel());
    }
}
