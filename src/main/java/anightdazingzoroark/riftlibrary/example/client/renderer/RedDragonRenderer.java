package anightdazingzoroark.riftlibrary.example.client.renderer;

import anightdazingzoroark.riftlibrary.example.client.model.RedDragonModel;
import anightdazingzoroark.riftlibrary.example.entity.RedDragonEntity;
import anightdazingzoroark.riftlibrary.main.renderer.RiftLibEntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RedDragonRenderer extends RiftLibEntityRenderer<RedDragonEntity> {
    public RedDragonRenderer(RenderManager renderManager) {
        super(renderManager, new RedDragonModel());
    }
}
