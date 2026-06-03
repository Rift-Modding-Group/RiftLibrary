package anightdazingzoroark.riftlib.proxy;

import anightdazingzoroark.example.CommonListener;
import anightdazingzoroark.example.client.model.entity.DragonModel;
import anightdazingzoroark.example.client.model.entity.FlyingPufferfishModel;
import anightdazingzoroark.example.entity.DragonEntity;
import anightdazingzoroark.example.entity.FlyingPufferfishEntity;
import anightdazingzoroark.example.entity.hitboxLinker.DragonHitboxLinker;
import anightdazingzoroark.example.entity.hitboxLinker.FlyingPufferfishHitboxLinker;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.hitbox.HitboxTicker;
import anightdazingzoroark.riftlib.internalMessage.*;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import anightdazingzoroark.riftlib.message.RiftLibMessageWrapper;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.model.ServerModelTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleComponentRegistry;
import anightdazingzoroark.riftlib.ray.RayTicker;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosTicker;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerProxy {
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> MESSAGE_WRAPPER;
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> HITBOX_MESSAGE_WRAPPER;
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> SERVER_MODEL_MESSAGE_WRAPPER;
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> RAY_MESSAGE_WRAPPER;

    public void preInit(FMLPreInitializationEvent e) {
        //registerVariable particle component registry
        RiftLibParticleComponentRegistry.initializeMap();

        //registerVariable internal messages
        MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID);
        MESSAGE_WRAPPER.registerMessage(RiftLibCreateParticle.class, RiftLibMessageSide.CLIENT);
        MESSAGE_WRAPPER.registerMessage(RiftLibPlaySoundForPlayer.class, RiftLibMessageSide.CLIENT);

        HITBOX_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_hitbox");
        HITBOX_MESSAGE_WRAPPER.registerMessage(RiftLibSyncHitboxEntityId.class, RiftLibMessageSide.CLIENT);

        SERVER_MODEL_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_server_model");
        SERVER_MODEL_MESSAGE_WRAPPER.registerMessage(RiftTickClientFromServer.class, RiftLibMessageSide.CLIENT);

        RAY_MESSAGE_WRAPPER = new RiftLibMessageWrapper<>(RiftLib.ModID+"_ray");
        RAY_MESSAGE_WRAPPER.registerMessage(RiftLibCreateOrDestroyRay.class, RiftLibMessageSide.BOTH);

        MinecraftForge.EVENT_BUS.register(new HitboxTicker.Server());
        MinecraftForge.EVENT_BUS.register(new RayTicker.Server());
        MinecraftForge.EVENT_BUS.register(new ServerModelTicker());
        MinecraftForge.EVENT_BUS.register(new DynamicRidePosTicker.Server());

        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            MinecraftForge.EVENT_BUS.register(new CommonListener());
            RiftLibLinkerRegistry.registerEntityHitboxLinker(DragonEntity.class, new DragonHitboxLinker());
            RiftLibLinkerRegistry.registerEntityHitboxLinker(FlyingPufferfishEntity.class, new FlyingPufferfishHitboxLinker());
            ServerModelRegistry.registerServerModel(DragonEntity.class, DragonModel::new);
            ServerModelRegistry.registerServerModel(FlyingPufferfishEntity.class, FlyingPufferfishModel::new);
        }
    }

    public void init(FMLInitializationEvent event) {
        RiftLibCacheServer.getInstance().load();
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

    public void spawnParticle(String name, double x, double y, double z) {}

    public void spawnParticle(String name, double x, double y, double z, double rotationX, double rotationY) {}
}
