package anightdazingzoroark;

import anightdazingzoroark.example.CommonListener;
import anightdazingzoroark.riftlib.RiftLibEvent;
import anightdazingzoroark.riftlib.internalMessage.RiftLibInternalMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@Mod.EventBusSubscriber
public class ServerProxy {
    public void preInit(FMLPreInitializationEvent e) {
        RiftLibInternalMessage.registerMessages();

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

    public <T extends RiftLibMessage<T>> void handleMessage(final T message, final MessageContext messageContext) {
        WorldServer world = (WorldServer) messageContext.getServerHandler().player.world;
        world.addScheduledTask(() -> message.executeOnServer(
                FMLCommonHandler.instance().getMinecraftServerInstance(),
                message,
                messageContext.getServerHandler().player,
                messageContext
        ));
    }
}
