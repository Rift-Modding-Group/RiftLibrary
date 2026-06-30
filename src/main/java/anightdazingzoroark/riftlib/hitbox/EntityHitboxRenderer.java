package anightdazingzoroark.riftlib.hitbox;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Make collision hitbox completely invisible
 * */
@SideOnly(Side.CLIENT)
public class EntityHitboxRenderer extends Render<RiftLibCollisionHitbox> {
    protected EntityHitboxRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(RiftLibCollisionHitbox entity) {
        return null;
    }

    public static class Factory implements IRenderFactory<RiftLibCollisionHitbox> {
        @Override
        public Render<? super RiftLibCollisionHitbox> createRenderFor(RenderManager manager) {
            return new EntityHitboxRenderer(manager);
        }
    }
}
