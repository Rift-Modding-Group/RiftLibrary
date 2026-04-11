package anightdazingzoroark.riftlib.proxy;

import anightdazingzoroark.example.CommonListener;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.hitbox.HitboxTicker;
import anightdazingzoroark.riftlib.internalMessage.*;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import anightdazingzoroark.riftlib.message.RiftLibMessageWrapper;
import anightdazingzoroark.riftlib.particle.RiftLibParticleComponentRegistry;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerProxy {
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> MESSAGE_WRAPPER;
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> HITBOX_MESSAGE_WRAPPER;

    public void preInit(FMLPreInitializationEvent e) {
        //register particle component registry
        RiftLibParticleComponentRegistry.initializeMap();

        //register internal messages
        MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID);
        MESSAGE_WRAPPER.registerMessage(RiftLibUpdateRiderPos.class, RiftLibMessageSide.BOTH);
        MESSAGE_WRAPPER.registerMessage(RiftLibCreateParticle.class, RiftLibMessageSide.CLIENT);
        MESSAGE_WRAPPER.registerMessage(RiftLibPlaySoundForPlayer.class, RiftLibMessageSide.CLIENT);

        HITBOX_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_hitbox");
        HITBOX_MESSAGE_WRAPPER.registerMessage(RiftLibUpdateHitboxPos.class, RiftLibMessageSide.BOTH);
        HITBOX_MESSAGE_WRAPPER.registerMessage(RiftLibUpdateHitboxSize.class, RiftLibMessageSide.BOTH);

        MinecraftForge.EVENT_BUS.register(new HitboxTicker.Server());

        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            MinecraftForge.EVENT_BUS.register(new CommonListener());
        }
    }

    public void init(FMLInitializationEvent event) {}

    public <T extends RiftLibMessage<T>> void handleMessage(final T message, final MessageContext messageContext) {
        WorldServer world = (WorldServer) messageContext.getServerHandler().player.world;
        world.addScheduledTask(() -> message.executeOnServer(
                FMLCommonHandler.instance().getMinecraftServerInstance(),
                message,
                messageContext.getServerHandler().player,
                messageContext
        ));
    }

    public void spawnParticle(String name, double x, double y, double z) {}

    public void spawnParticle(String name, double x, double y, double z, double rotationX, double rotationY) {}
}
