package anightdazingzoroark.riftlibrary.main.proxy;

import anightdazingzoroark.riftlibrary.example.CommonListener;
import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        if (RiftLibraryMod.DEOBF_ENVIRONMENT && !RiftLibraryMod.DISABLE_IN_DEV) {
            MinecraftForge.EVENT_BUS.register(new CommonListener());
        }
    }
}
