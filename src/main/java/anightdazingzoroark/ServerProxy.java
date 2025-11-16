package anightdazingzoroark;

import anightdazingzoroark.example.CommonListener;
import anightdazingzoroark.riftlib.RiftLibEvent;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod.EventBusSubscriber
public class ServerProxy {
    public void preInit(FMLPreInitializationEvent e) {
        RiftLibMessage.registerMessages();

        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            MinecraftForge.EVENT_BUS.register(new CommonListener());
        }
    }

    public void init(FMLInitializationEvent event) {
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            MinecraftForge.EVENT_BUS.register(new RiftLibEvent());
        }
    }
}
