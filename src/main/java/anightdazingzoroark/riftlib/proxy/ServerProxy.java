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
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> INSTRUCTION_MESSAGE_WRAPPER;
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> RAY_MESSAGE_WRAPPER;

    public void preInit(FMLPreInitializationEvent e) {
        //registerVariable particle component registry
        RiftLibParticleComponentRegistry.initializeMap();

        //registerVariable internal messages
        MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID);
        MESSAGE_WRAPPER.registerMessage(RiftLibUpdateRiderPos.class, RiftLibMessageSide.BOTH);
        MESSAGE_WRAPPER.registerMessage(RiftLibCreateParticle.class, RiftLibMessageSide.CLIENT);
        MESSAGE_WRAPPER.registerMessage(RiftLibPlaySoundForPlayer.class, RiftLibMessageSide.CLIENT);

        HITBOX_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_hitbox");
        HITBOX_MESSAGE_WRAPPER.registerMessage(RiftLibUpdateHitboxPos.class, RiftLibMessageSide.BOTH);
        HITBOX_MESSAGE_WRAPPER.registerMessage(RiftLibUpdateHitboxSize.class, RiftLibMessageSide.BOTH);

        INSTRUCTION_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_instruction");
        INSTRUCTION_MESSAGE_WRAPPER.registerMessage(RiftLibRunAnimationMessageEffect.class, RiftLibMessageSide.BOTH);

        RAY_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_ray");
        RAY_MESSAGE_WRAPPER.registerMessage(RiftLibCreateOrDestroyRay.class, RiftLibMessageSide.CLIENT);
        RAY_MESSAGE_WRAPPER.registerMessage(RiftLibCreateRayInServer.class, RiftLibMessageSide.SERVER);

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
