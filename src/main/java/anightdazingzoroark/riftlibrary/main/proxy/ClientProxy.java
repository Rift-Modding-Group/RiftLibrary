package anightdazingzoroark.riftlibrary.main.proxy;

import anightdazingzoroark.riftlibrary.example.CommonListener;
import anightdazingzoroark.riftlibrary.example.client.renderer.RedDragonRenderer;
import anightdazingzoroark.riftlibrary.example.entity.RedDragonEntity;
import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        if (RiftLibraryMod.DEOBF_ENVIRONMENT && !RiftLibraryMod.DISABLE_IN_DEV) {
            RenderingRegistry.registerEntityRenderingHandler(RedDragonEntity.class, RedDragonRenderer::new);
        }
    }
}
